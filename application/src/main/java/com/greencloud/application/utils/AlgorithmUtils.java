package com.greencloud.application.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.greencloud.application.domain.job.Job;
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
	 * @param jobList   list of the job of interest
	 * @param startTime start time of the interval
	 * @param endTime   end time of the interval
	 */
	public static int getMaximumUsedPowerDuringTimeStamp(final Set<Job> jobList, final Instant startTime,
			final Instant endTime) {
		final List<Job> jobsWithinInterval = jobList.stream()
				.filter(job -> job.getStartTime().isBefore(endTime) && job.getEndTime().isAfter(startTime))
				.toList();
		final List<JobWithTime> jobsWithTimeMap = jobsWithinInterval.stream()
				.map(job -> mapToJobWithTime(job, startTime, endTime))
				.flatMap(List::stream)
				.sorted(AlgorithmUtils::compareJobs)
				.toList();

		final List<Job> openIntervalJobs = new ArrayList<>();
		final List<Integer> powerInIntervals = new ArrayList<>();
		final AtomicInteger lastIntervalPower = new AtomicInteger(0);

		jobsWithTimeMap.forEach(jobWithTime -> {
			if (jobWithTime.timeType.equals(JobWithTime.TimeType.START_TIME)) {
				openIntervalJobs.add(jobWithTime.job);
				lastIntervalPower.updateAndGet(power -> power + jobWithTime.job.getPower());
			} else {
				openIntervalJobs.remove(jobWithTime.job);
				powerInIntervals.add(lastIntervalPower.get());
				lastIntervalPower.set(
						openIntervalJobs.isEmpty() ? 0 : lastIntervalPower.get() - jobWithTime.job.getPower());
			}
		});

		return powerInIntervals.stream()
				.max(Comparator.comparingInt(Integer::intValue))
				.orElse(0);
	}

	/**
	 * Method retrieves from the list of jobs, the ones which summed power will be the closest to the finalPower
	 *
	 * @param jobs       list of jobs to go through
	 * @param finalPower power bound
	 * @param type       type of the list objects
	 * @return list of jobs withing power bound
	 */
	public static <T> List<T> findJobsWithinPower(final List<?> jobs, final double finalPower, final Class<T> type) {
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
				final int power = type.equals(PowerJob.class) ? ((PowerJob) job).getPower() : ((Job) job).getPower();
				final SubJobList newSum = new SubJobList(sum.size + power, newSubList);

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

	private static List<JobWithTime> mapToJobWithTime(final Job job, final Instant startTime,
			final Instant endTime) {
		final Instant realStart = job.getStartTime().isBefore(startTime) ? startTime : job.getStartTime();
		final Instant realEnd = job.getEndTime().isAfter(endTime) ? endTime : job.getEndTime();

		return List.of(
				new JobWithTime(job, realStart, JobWithTime.TimeType.START_TIME),
				new JobWithTime(job, realEnd, JobWithTime.TimeType.END_TIME));
	}

	private static int compareJobs(final JobWithTime job1, final JobWithTime job2) {
		final int comparingTimeResult = job1.time.compareTo(job2.time);

		if (job1.job.equals(job2.job) && comparingTimeResult == 0) {
			return job1.timeType.equals(JobWithTime.TimeType.START_TIME) ? 1 : -1;
		}
		return comparingTimeResult;
	}

}
