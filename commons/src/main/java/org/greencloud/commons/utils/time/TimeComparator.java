package org.greencloud.commons.utils.time;

import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;

import java.time.Instant;

import org.greencloud.commons.domain.job.basic.PowerJob;

/**
 * Class contain methods used to compare time frames
 */
public class TimeComparator {

	/**
	 * Method checks if the given time is within given timestamp
	 *
	 * @param timeStampStart start of the time stamp
	 * @param timeStampEnd   end of the time stamp
	 * @param timeToCheck    time which has to be checked
	 * @return true or false value
	 */
	public static boolean isWithinTimeStamp(final Instant timeStampStart,
			final Instant timeStampEnd,
			final Instant timeToCheck) {
		return !timeToCheck.isBefore(timeStampStart) && timeToCheck.isBefore(timeStampEnd);
	}

	/**
	 * Method checks if the given job is within timestamp
	 *
	 * @param job         job for which time will be checked
	 * @param timeToCheck time which has to be checked
	 * @return true or false value
	 */
	public static boolean isWithinSimulatedTimeStamp(final PowerJob job, final Instant timeToCheck) {
		return !timeToCheck.isBefore(requireNonNull(job.getStartTime())) &&
				timeToCheck.isBefore(job.getExpectedEndTime());
	}

	/**
	 * Method checks if the given time (in real time) is within job timestamp
	 *
	 * @param job         job for which time will be checked
	 * @param timeToCheck time which has to be checked
	 * @return true or false value
	 */
	public static boolean isWithinTimeStamp(final PowerJob job, final Instant timeToCheck) {
		return !timeToCheck.isBefore(convertToRealTime(job.getStartTime()))
				&& timeToCheck.isBefore(convertToRealTime(job.getExpectedEndTime()));
	}
}
