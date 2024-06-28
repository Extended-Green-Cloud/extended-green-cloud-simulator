package org.greencloud.commons.utils.time;

import static java.util.Objects.requireNonNull;

import java.time.Instant;

import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;

/**
 * Class contain methods used to postpone selected time
 */
public class TimeScheduler {

	/**
	 * Method aligns a given start time by comparing it to the current time
	 * (i.e. if the start time has already passed then it substitutes it with current time)
	 *
	 * @param startTime initial start time
	 * @return Instant being an aligned start time
	 */
	public static Instant alignStartTimeToCurrentTime(final Instant startTime) {
		return alignStartTimeToSelectedTime(startTime, TimeSimulation.getCurrentTime());
	}

	/**
	 * Method aligns a given start time by comparing it to the relevant time instant
	 * (i.e. if the given time instant has passed the start time then it substitutes it with it)
	 *
	 * @param startTime initial start time
	 * @return Instant being an aligned start time
	 */
	public static Instant alignStartTimeToSelectedTime(final Instant startTime, final Instant relevantTime) {
		return relevantTime.isAfter(startTime) ? relevantTime : startTime;
	}

	/**
	 * Methods computes the finish time of the job.
	 *
	 * @param executionEstimation estimation of job execution
	 * @return expected completion time
	 */
	public static Instant computeFinishTime(final JobWithExecutionEstimation executionEstimation) {
		return requireNonNull(executionEstimation.getEarliestStartTime())
				.plusMillis(executionEstimation.getEstimatedDuration());
	}
}
