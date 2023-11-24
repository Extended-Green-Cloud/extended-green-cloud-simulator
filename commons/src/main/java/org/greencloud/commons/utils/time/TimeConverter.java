package org.greencloud.commons.utils.time;

import static java.time.Duration.between;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.greencloud.commons.constants.TimeConstants.MILLISECOND_MULTIPLIER;
import static org.greencloud.commons.constants.TimeConstants.MILLIS_IN_MIN;
import static org.greencloud.commons.constants.TimeConstants.MINUTES_IN_HOUR;
import static org.greencloud.commons.constants.TimeConstants.SECONDS_IN_HOUR;
import static org.greencloud.commons.constants.TimeConstants.SECONDS_PER_HOUR;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

import org.greencloud.commons.exception.IncorrectTaskDateException;
import org.slf4j.Logger;

/**
 * Class contain methods used to convert time into different objects
 */
public class TimeConverter {

	private static final Logger logger = getLogger(TimeConverter.class);
	private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

	/**
	 * Mapper used to convert the date written as string to the instant date
	 *
	 * @param date string date to be converted to offset date type
	 * @return Instant date
	 */
	public static Instant convertToInstantTime(final String date) {
		try {
			final LocalDateTime datetime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
			final ZonedDateTime zoned = datetime.atZone(ZoneId.of("UTC"));
			return zoned.toInstant();
		} catch (DateTimeParseException e) {
			logger.info("The provided date format is incorrect");
			e.printStackTrace();
			throw new IncorrectTaskDateException();
		}
	}

	/**
	 * Method converts milliseconds to string of the form "x min. y sec. z ms."
	 *
	 * @param ms number of milliseconds
	 * @return formatted string
	 */
	public static String convertMillisecondsToTimeString(final long ms) {
		final long h = TimeUnit.MILLISECONDS.toHours(ms);
		final long min = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(h);
		final long sec =
				TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(min) - TimeUnit.HOURS.toMinutes(h);
		final long msRest =
				ms - (TimeUnit.MINUTES.toMillis(min) + TimeUnit.MINUTES.toMillis(sec) + TimeUnit.HOURS.toMinutes(h));

		if (h != 0) {
			return String.format("%02d h %02d min. %02d sec. %02d ms.", h, min, sec, msRest);
		}
		if (min != 0) {
			return String.format("%02d min. %02d sec. %02d ms.", min, sec, msRest);
		}
		if (sec != 0) {
			return String.format("%02d sec. %02d ms.", sec, msRest);
		}
		return String.format("%02d ms.", msRest);
	}

	/**
	 * Method converts the current simulation time into the real time
	 *
	 * @param time time which is representing simulation time
	 * @return Instant being a time representing a real time
	 */
	public static Instant convertToRealTime(final Instant time) {
		final long simulationTimeDifference = between(TimeSimulation.SYSTEM_START_TIME, time).toMillis();
		final double realTimeMultiplier = (double) SECONDS_IN_HOUR / SECONDS_PER_HOUR;
		final double realTimeDifference = simulationTimeDifference * realTimeMultiplier;
		return TimeSimulation.SYSTEM_START_TIME.plusMillis((long) realTimeDifference);
	}

	/**
	 * Method converts the current simulation time into the real time
	 *
	 * @param millis time in milliseconds
	 * @return time in minutes of real time
	 */
	public static long convertToRealTime(final long millis) {
		final double realTimeMultiplier = (double) MINUTES_IN_HOUR / (SECONDS_PER_HOUR * MILLISECOND_MULTIPLIER);
		final double realTimeDifference = millis * realTimeMultiplier;
		return (long) realTimeDifference;
	}

	/**
	 * Method converts the current simulation time into the real time
	 *
	 * @param millis time in milliseconds
	 * @return time in millis of real time
	 */
	public static long convertToRealTimeMillis(final long millis) {
		return convertToRealTime(millis) * MILLIS_IN_MIN;
	}

	/**
	 * Method converts number of seconds to milliseconds in simulation time
	 *
	 * @param seconds number of seconds
	 * @return time in milliseconds
	 */
	public static long convertToSimulationTime(final long seconds) {
		return (long) (((double) seconds / SECONDS_IN_HOUR) * SECONDS_PER_HOUR * MILLISECOND_MULTIPLIER);
	}

	/**
	 * Method computes difference in hours between two dates
	 *
	 * @param startTime time interval start time
	 * @param endTime   time interval end time
	 * @return time in hours
	 */
	public static double convertToHourDuration(final Instant startTime, final Instant endTime) {
		return (double) SECONDS.between(startTime, endTime) / SECONDS_PER_HOUR;
	}

	/**
	 * Method computes converts milliseconds to hours
	 *
	 * @param duration duration in milliseconds
	 * @return duration in hours
	 */
	public static double convertMillisToHoursRealTime(final long duration) {
		return ((double) duration / (MILLISECOND_MULTIPLIER * SECONDS_PER_HOUR));
	}
}
