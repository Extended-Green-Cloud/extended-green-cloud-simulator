package com.greencloud.application.utils;

import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.SUB_INTERVAL_ERROR;
import static com.greencloud.application.utils.TimeUtils.divideIntoSubIntervals;
import static com.greencloud.application.utils.domain.JobWithTime.TimeType.START_TIME;
import static java.lang.Math.max;
import static java.time.Duration.between;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import com.google.common.util.concurrent.AtomicDouble;
import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.application.domain.weather.MonitoringData;
import com.greencloud.application.utils.domain.JobWithTime;
import com.greencloud.application.utils.domain.SubJobList;
import com.greencloud.commons.domain.job.PowerJob;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.greencloud.commons.domain.resources.ImmutableHardwareResources;

/**
 * Service used to perform operations using more complex algorithms
 */
public class AlgorithmUtils {

	private static final long MILLIS_IN_MIN = 60000L;

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
	 * @param jobList              list of the jobs of interest (that uses real times instead of simulation times)
	 * @param startTime            start time of the interval (in real time)
	 * @param endTime              end time of the interval (in real time)
	 * @param intervalLength       length of single sub-interval in minutes
	 * @param maximumCapacity      current maximum capacity of given source
	 * @param greenPowerManagement manager that will compute available capacity
	 * @param monitoringData       weather data necessary to compute available capacity
	 */
	public static double getMinimalAvailableEnergyDuringTimeStamp(
			final Set<ServerJob> jobList,
			final Instant startTime,
			final Instant endTime,
			final long intervalLength,
			final GreenPowerManagement greenPowerManagement,
			final double maximumCapacity,
			final MonitoringData monitoringData) {
		final List<JobWithTime<ServerJob>> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final Deque<Map.Entry<Instant, Double>> powerInIntervals = jobsWithTimeMap.isEmpty() ? new ArrayDeque<>() :
				getEnergyForJobIntervals(jobsWithTimeMap.subList(0, jobsWithTimeMap.size() - 1));
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength * MILLIS_IN_MIN);

		final AtomicReference<Double> minimumAvailableEnergy = new AtomicReference<>(maximumCapacity);
		final AtomicReference<Map.Entry<Instant, Double>> lastOpenedPowerInterval = new AtomicReference<>(null);

		subIntervals.forEach(time -> {
			while (!powerInIntervals.isEmpty() && !powerInIntervals.peekFirst().getKey().isAfter(time)) {
				lastOpenedPowerInterval.set(powerInIntervals.removeFirst());
			}
			final double availableCapacity = greenPowerManagement.getAvailableGreenEnergy(monitoringData, time);
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

	/**
	 * Method computes the probability that the given maximum value is incorrect
	 * (it was assumed that the smallest time interval is equal to 10 min)
	 *
	 * @param startTime      start time of the interval (in real time)
	 * @param endTime        end time of the interval (in real time)
	 * @param intervalLength length of single sub-interval in minutes
	 * @return margin of error
	 */
	public static double computeIncorrectMaximumValProbability(final Instant startTime, final Instant endTime,
			final long intervalLength) {
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength * MILLIS_IN_MIN);
		final long sampleSize = (long) subIntervals.size() - 1;
		final double populationSize = (double) between(startTime, endTime).toMinutes() / 10;

		return SUB_INTERVAL_ERROR + max(1 - sampleSize / populationSize, 0);

	}

	/**
	 * Method computes the next number in the Fibonacci sequence
	 *
	 * @param n previous number
	 */
	public static int nextFibonacci(int n) {
		double a = n * (1 + Math.sqrt(5)) / 2.0;
		return (int) Math.round(a);
	}

	/**
	 * Method computes the previous number in the Fibonacci sequence
	 *
	 * @param n current number
	 */
	public static int previousFibonacci(int n) {
		double a = n / ((1 + Math.sqrt(5)) / 2.0);
		return (int) Math.round(a);
	}

	/**
	 * Method uses apache.math3.stat to compute Kendall's Tau coefficient used to check the correlation between
	 * time and variable
	 *
	 * @param timeInstances time instances when the values were computed
	 * @param values        computed values
	 * @return correlation coefficient
	 */
	public static double computeKendallTau(final List<Instant> timeInstances, final List<Double> values) {
		final double[] timeValues = timeInstances.stream().mapToDouble(Instant::toEpochMilli).toArray();
		final double[] valueArray = values.stream().mapToDouble(value -> value).toArray();

		return new KendallsCorrelation().correlation(timeValues, valueArray);
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
				.sorted(AlgorithmUtils::compareJobs)
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
