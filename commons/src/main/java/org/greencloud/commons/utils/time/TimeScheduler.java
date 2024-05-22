package org.greencloud.commons.utils.time;

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
}
