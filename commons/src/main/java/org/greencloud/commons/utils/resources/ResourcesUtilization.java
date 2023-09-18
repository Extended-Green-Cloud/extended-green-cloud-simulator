package org.greencloud.commons.utils.resources;

import static org.greencloud.commons.constants.TimeConstants.MILLIS_IN_MIN;
import static org.greencloud.commons.utils.resources.domain.JobWithTime.TimeType.START_TIME;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.greencloud.commons.domain.resources.ImmutableHardwareResources;
import org.greencloud.commons.domain.weather.MonitoringData;

import com.google.common.util.concurrent.AtomicDouble;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.utils.resources.domain.JobWithTime;
import org.greencloud.commons.utils.resources.domain.SubJobList;

/**
 * Class with algorithms used to compute resource utilization
 */
public class ResourcesUtilization {

	/**
	 * Method computes the maximum resource usage during given time-stamp
	 *
	 * @param jobList   list of the jobs of interest
	 * @param startTime start time of the interval
	 * @param endTime   end time of the interval
	 */
	public static <T extends PowerJob> HardwareResources getMaximumUsedResourcesDuringTimeStamp(
			final Set<T> jobList,
			final Instant startTime,
			final Instant endTime) {
		final List<JobWithTime<T>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		if (jobsWithTimeMap.isEmpty()) {
			return ImmutableHardwareResources.builder().cpu(0D).memory(0D).storage(0D).build();
		}

		final List<T> openIntervalJobs = new ArrayList<>();

		final List<Double> cpuUsageInInterval = new ArrayList<>();
		final List<Double> memoryUsageInInterval = new ArrayList<>();
		final List<Double> storageUsageInInterval = new ArrayList<>();

		final AtomicDouble lastIntervalCpu = new AtomicDouble(0D);
		final AtomicDouble lastIntervalMemory = new AtomicDouble(0D);
		final AtomicDouble lastIntervalStorage = new AtomicDouble(0D);

		jobsWithTimeMap.forEach(jobWithTime -> {
			final HardwareResources resources = (jobWithTime.job).getEstimatedResources();
			if (jobWithTime.timeType.equals(START_TIME)) {
				openIntervalJobs.add(jobWithTime.job);

				lastIntervalCpu.updateAndGet(cpu -> cpu + resources.getCpu());
				lastIntervalMemory.updateAndGet(memory -> memory + resources.getMemory());
				lastIntervalStorage.updateAndGet(storage -> storage + resources.getStorage());
			} else {
				openIntervalJobs.remove(jobWithTime.job);

				cpuUsageInInterval.add(lastIntervalCpu.get());
				memoryUsageInInterval.add(lastIntervalMemory.get());
				storageUsageInInterval.add(lastIntervalStorage.get());

				lastIntervalCpu.set(openIntervalJobs.isEmpty() ? 0 : lastIntervalCpu.get() - resources.getCpu());
				lastIntervalMemory.set(
						openIntervalJobs.isEmpty() ? 0 : lastIntervalMemory.get() - resources.getMemory());
				lastIntervalStorage.set(
						openIntervalJobs.isEmpty() ? 0 : lastIntervalStorage.get() - resources.getStorage());
			}
		});

		final double maxCpu = Collections.max(cpuUsageInInterval, Double::compareTo);
		final double maxMemory = Collections.max(memoryUsageInInterval, Double::compareTo);
		final double maxStorage = Collections.max(storageUsageInInterval, Double::compareTo);

		return ImmutableHardwareResources.builder().cpu(maxCpu).memory(maxMemory).storage(maxStorage).build();
	}

	/**
	 * Method computes the minimized available power during specific time-stamp.
	 * The momentum available power is a difference between available green power and the power in use at the
	 * specific moment
	 * <p/>
	 * IMPORTANT! All time frames used in the calculation refer to the real time (not simulation time). The main
	 * reason for that is that the weather forecast requires real job execution time
	 *
	 * @param jobList        list of the jobs of interest (that uses real times instead of simulation times)
	 * @param startTime      start time of the interval (in real time)
	 * @param endTime        end time of the interval (in real time)
	 * @param intervalLength length of single sub-interval in minutes
	 * @param agentProps     manager properties of green energy agent
	 * @param monitoringData weather data necessary to compute available capacity
	 */
	public static double getMinimalAvailableEnergyDuringTimeStamp(
			final Set<ServerJob> jobList,
			final Instant startTime,
			final Instant endTime,
			final long intervalLength,
			final GreenEnergyAgentProps agentProps,
			final MonitoringData monitoringData) {
		final List<JobWithTime<ServerJob>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final Deque<Map.Entry<Instant, Double>> powerInIntervals = jobsWithTimeMap.isEmpty() ? new ArrayDeque<>() :
				getEnergyForJobIntervals(jobsWithTimeMap.subList(0, jobsWithTimeMap.size() - 1));
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength * MILLIS_IN_MIN);

		final AtomicReference<Double> minimumAvailableEnergy = new AtomicReference<>(
				(double) agentProps.getMaximumGeneratorCapacity());
		final AtomicReference<Map.Entry<Instant, Double>> lastOpenedPowerInterval = new AtomicReference<>(null);

		subIntervals.forEach(time -> {
			while (!powerInIntervals.isEmpty() && !powerInIntervals.peekFirst().getKey().isAfter(time)) {
				lastOpenedPowerInterval.set(powerInIntervals.removeFirst());
			}
			final double availableCapacity = agentProps.getAvailableGreenEnergy(monitoringData, time);
			final double powerInUse = nonNull(lastOpenedPowerInterval.get()) ?
					lastOpenedPowerInterval.get().getValue() : 0;
			final double availablePower = availableCapacity - powerInUse;

			if (availablePower >= 0 && availablePower < minimumAvailableEnergy.get()) {
				minimumAvailableEnergy.set(availablePower);
			}
		});

		return minimumAvailableEnergy.get();
	}

	/**
	 * Method divides the given interval into sub-intervals of specified size
	 *
	 * @param startTime time interval start time
	 * @param endTime   time interval end time
	 * @param length    length of sub-interval
	 * @return list of sub-intervals represented by their start times
	 */
	public static Set<Instant> divideIntoSubIntervals(final Instant startTime, final Instant endTime,
			final Long length) {
		final AtomicReference<Instant> currentTime = new AtomicReference<>(startTime);
		final Set<Instant> subIntervals = new LinkedHashSet<>();

		do {
			subIntervals.add(currentTime.get());
			currentTime.getAndUpdate(time -> time.plusMillis(length));
		} while (currentTime.get().isBefore(endTime) && length != 0);

		subIntervals.add(endTime);

		return subIntervals;
	}

	/**
	 * Method retrieves from the list of jobs, the ones which summed power will be the closest to the finalPower
	 *
	 * @param jobs       list of jobs to go through
	 * @param finalPower power bound
	 * @return list of jobs withing power bound
	 */
	public static List<ServerJob> findJobsWithinPower(final List<ServerJob> jobs, final double finalPower) {
		if (finalPower == 0) {
			return emptyList();
		}
		final AtomicReference<SubJobList<ServerJob>> result = new AtomicReference<>(new SubJobList<>());
		final Set<SubJobList<ServerJob>> sums = new HashSet<>();

		sums.add(result.get());
		jobs.forEach(job -> {
			final Set<SubJobList<ServerJob>> newSums = new HashSet<>();
			sums.forEach(sum -> {
				final List<ServerJob> newSubList = new ArrayList<>(sum.subList);
				newSubList.add(job);
				final SubJobList<ServerJob> newSum = new SubJobList<>(
						sum.energySum + job.getEstimatedEnergy(), newSubList);

				if (newSum.energySum <= finalPower) {
					newSums.add(newSum);
					if (newSum.energySum > result.get().energySum) {
						result.set(newSum);
					}
				}
			});
			sums.addAll(newSums);
		});
		return result.get().subList;
	}

	private static Deque<Map.Entry<Instant, Double>> getEnergyForJobIntervals(
			final List<JobWithTime<ServerJob>> jobsWithTimeMap) {
		final Deque<Map.Entry<Instant, Double>> powerInIntervals = new ArrayDeque<>();
		final AtomicDouble lastIntervalPower = new AtomicDouble(0D);

		jobsWithTimeMap.forEach(jobWithTime -> {
			final double energy = jobWithTime.job.getEstimatedEnergy();
			if (jobWithTime.timeType.equals(START_TIME)) {
				lastIntervalPower.updateAndGet(power -> power + energy);
			} else {
				lastIntervalPower.updateAndGet(power -> power - energy);
			}
			powerInIntervals.removeIf(entry -> entry.getKey().equals(jobWithTime.time));
			powerInIntervals.addLast(Map.entry(jobWithTime.time, lastIntervalPower.get()));
		});
		return powerInIntervals;
	}

	private static <T extends PowerJob> List<JobWithTime<T>> getJobsWithTimesForInterval(final Set<T> jobList,
			final Instant startTime, final Instant endTime) {
		final List<T> jobsWithinInterval = jobList.stream()
				.filter(job -> job.getStartTime().isBefore(endTime) && job.getEndTime().isAfter(startTime))
				.toList();
		return jobsWithinInterval.stream()
				.map(job -> mapToJobWithTime(job, startTime, endTime))
				.flatMap(List::stream)
				.sorted(ResourcesUtilization::compareJobs)
				.toList();
	}

	private static <T extends PowerJob> List<JobWithTime<T>> mapToJobWithTime(final T job, final Instant startTime,
			final Instant endTime) {
		final Instant realStart = job.getStartTime().isBefore(startTime) ? startTime : job.getStartTime();
		final Instant realEnd = job.getEndTime().isAfter(endTime) ? endTime : job.getEndTime();

		return List.of(
				new JobWithTime<>(job, realStart, START_TIME),
				new JobWithTime<>(job, realEnd, JobWithTime.TimeType.END_TIME));
	}

	private static <T extends PowerJob> int compareJobs(final JobWithTime<T> job1, final JobWithTime<T> job2) {
		final int comparingTimeResult = job1.time.compareTo(job2.time);

		if (job1.job.equals(job2.job) && comparingTimeResult == 0) {
			return job1.timeType.equals(START_TIME) ? 1 : -1;
		}
		return comparingTimeResult;
	}
}
