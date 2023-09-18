package org.greencloud.commons.utils.time;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Instant;

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
	 * Method computes new time by postponing the previous one by given (in real time) minutes amount
	 *
	 * @param time    time to be postponed
	 * @param minutes minutes used to postpone the time
	 * @return Instant being the postponed time
	 */
	public static Instant postponeTime(final Instant time, final long minutes) {
		final long simulationAdjustment = TimeConverter.convertToSimulationTime(minutes * 60);
		return time.plus(simulationAdjustment, MILLIS);
	}
}
