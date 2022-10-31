package com.greencloud.application.utils;

import static com.greencloud.application.utils.TimeUtils.divideIntoSubIntervals;
import static com.greencloud.application.utils.domain.JobWithTime.TimeType.START_TIME;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.utils.domain.JobWithTime;
import com.greencloud.application.utils.domain.SubJobList;

/**
 * Service used to perform operations using already existing, implemented algorithms
 */
public class AlgorithmUtils {

	/**
	 * Method computes the maximum power which will be used by the jobs during given time-stamp
	 * (full algorithm description can be found on project's Wiki)
	 *
	 * @param jobList   list of the jobs of interest
	 * @param startTime start time of the interval
	 * @param endTime   end time of the interval
	 */
	public static <T extends PowerJob, N extends JobWithTime> int getMaximumUsedPowerDuringTimeStamp(
			final Set<T> jobList,
			final Instant startTime,
			final Instant endTime) {
		final List<N> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final List<T> openIntervalJobs = new ArrayList<>();
		final List<Integer> powerInIntervals = new ArrayList<>();
		final AtomicInteger lastIntervalPower = new AtomicInteger(0);

		jobsWithTimeMap.forEach(jobWithTime -> {
			if (jobWithTime.timeType.equals(START_TIME)) {
				openIntervalJobs.add((T) jobWithTime.job);
				lastIntervalPower.updateAndGet(power -> power + ((PowerJob) jobWithTime.job).getPower());
			} else {
				openIntervalJobs.remove(jobWithTime.job);
				powerInIntervals.add(lastIntervalPower.get());
				lastIntervalPower.set(
						openIntervalJobs.isEmpty() ? 0 : lastIntervalPower.get() - ((PowerJob) jobWithTime.job).getPower());
			}
		});

		return powerInIntervals.stream()
				.max(Comparator.comparingInt(Integer::intValue))
				.orElse(0);
	}

	/**
	 * Method computes the minimized available power during specific time-stamp.
	 * The momentum available power is a difference between available green power and the power in use at
	 * specific moment
	 *
	 * @param jobList              list of the jobs of interest
	 * @param startTime            start time of the interval
	 * @param endTime              end time of the interval
	 * @param intervalLength       length of single sub-interval
	 * @param greenPowerManagement manager that will compute available capacity
	 * @param monitoringData       weather data necessary to compute available capacity
	 */
	public static <T extends PowerJob, N extends JobWithTime> double getMinimalAvailablePowerDuringTimeStamp(
			final Set<T> jobList, final Instant startTime, final Instant endTime, final long intervalLength,
			final GreenPowerManagement greenPowerManagement, final MonitoringData monitoringData) {
		final List<N> jobsWithTimeMap = getJobsWithTimesForInterval(jobList, startTime, endTime);

		final Deque<Map.Entry<Instant, Integer>> powerInIntervals = jobsWithTimeMap.isEmpty() ?
				new ArrayDeque<>() :
				getPowerForJobIntervals(jobsWithTimeMap.subList(0, jobsWithTimeMap.size() - 1));
		final Set<Instant> subIntervals = divideIntoSubIntervals(startTime, endTime, intervalLength);
		final AtomicReference<Double> minimumAvailablePower = new AtomicReference<>((double) greenPowerManagement.getCurrentMaximumCapacity());
		final AtomicReference<Map.Entry<Instant, Integer>> lastOpenedPowerInterval = new AtomicReference<>(null);

		subIntervals.stream()
				.forEach(time -> {
					while (!powerInIntervals.isEmpty() && !powerInIntervals.peekFirst().getKey().isAfter(time)) {
						lastOpenedPowerInterval.set(powerInIntervals.removeFirst());
					}
					final double availableCapacity = greenPowerManagement.getAvailablePower(monitoringData, time);
					final double powerInUse = nonNull(lastOpenedPowerInterval.get()) ?
							lastOpenedPowerInterval.get().getValue() :
							0;
					final double availablePower = availableCapacity - powerInUse;

					if (availablePower >= 0 && availablePower < minimumAvailablePower.get()) {
						minimumAvailablePower.set(availablePower);
					}
				});

		return minimumAvailablePower.get();
	}

	/**
	 * Method retrieves from the list of jobs, the ones which summed power will be the closest to the finalPower
	 *
	 * @param jobs       list of jobs to go through
	 * @param finalPower power bound
	 * @return list of jobs withing power bound
	 */
	public static <T extends PowerJob> List<T> findJobsWithinPower(final List<T> jobs, final double finalPower) {
		if (finalPower == 0) {
			return Collections.emptyList();
		}

		final AtomicReference<SubJobList> result = new AtomicReference<>(new SubJobList());

		final Set<SubJobList> sums = new HashSet<>();
		sums.add(result.get());

		jobs.forEach(job -> {
			final Set<SubJobList> newSums = new HashSet<>();
			sums.forEach(sum -> {
				final List<T> newSubList = new ArrayList<>((Collection<? extends T>) sum.subList);
				newSubList.add((T) job);
				final SubJobList newSum = new SubJobList(sum.size + job.getPower(), newSubList);

				if (newSum.size <= finalPower) {
					newSums.add(newSum);
					if (newSum.size > result.get().size) {
						result.set(newSum);
					}
				}
			});
			sums.addAll(newSums);
		});
		return (List<T>) result.get().subList;
	}

	private static Deque<Map.Entry<Instant, Integer>> getPowerForJobIntervals(
			final List<? extends JobWithTime> jobsWithTimeMap) {
		final Deque<Map.Entry<Instant, Integer>> powerInIntervals = new ArrayDeque<>();
		final AtomicInteger lastIntervalPower = new AtomicInteger(0);

		jobsWithTimeMap.forEach(jobWithTime -> {
			if (jobWithTime.timeType.equals(START_TIME)) {
				lastIntervalPower.updateAndGet(power -> power + ((PowerJob)jobWithTime.job).getPower());
			} else {
				lastIntervalPower.updateAndGet(power -> power - ((PowerJob)jobWithTime.job).getPower());
			}
			powerInIntervals.removeIf(entry -> entry.getKey().equals(jobWithTime.time));
			powerInIntervals.addLast(Map.entry(jobWithTime.time, lastIntervalPower.get()));
		});
		return powerInIntervals;
	}

	private static <T extends PowerJob, N extends JobWithTime> List<N> getJobsWithTimesForInterval(final Set<T> jobList,
			final Instant startTime, final Instant endTime) {
		final List<T> jobsWithinInterval = jobList.stream()
				.filter(job -> job.getStartTime().isBefore(endTime) && job.getEndTime().isAfter(startTime))
				.toList();
		return (List<N>) jobsWithinInterval.stream()
				.map(job -> mapToJobWithTime(job, startTime, endTime))
				.flatMap(List::stream)
				.sorted(AlgorithmUtils::compareJobs)
				.toList();
	}

	private static <T extends PowerJob> List<JobWithTime> mapToJobWithTime(final T job, final Instant startTime,
			final Instant endTime) {
		final Instant realStart = job.getStartTime().isBefore(startTime) ? startTime : job.getStartTime();
		final Instant realEnd = job.getEndTime().isAfter(endTime) ? endTime : job.getEndTime();

		return List.of(
				new JobWithTime(job, realStart, START_TIME),
				new JobWithTime(job, realEnd, JobWithTime.TimeType.END_TIME));
	}

	private static int compareJobs(final JobWithTime job1, final JobWithTime job2) {
		final int comparingTimeResult = job1.time.compareTo(job2.time);

		if (job1.job.equals(job2.job) && comparingTimeResult == 0) {
			return job1.timeType.equals(START_TIME) ? 1 : -1;
		}
		return comparingTimeResult;
	}

}
