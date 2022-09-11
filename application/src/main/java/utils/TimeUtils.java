package utils;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exception.IncorrectTaskDateException;

/**
 * Service used to perform operations on date and time structures.
 */
public class TimeUtils {

	private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);
	private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
	private static final Long TIME_ERROR = 5L;
	private static final int MILLISECOND_MULTIPLIER = 1000;
	private static final int HOUR_DIVIDER = 3600;
	private static final int SECONDS_FOR_HOUR = 3;
	private static Clock CLOCK = Clock.systemDefaultZone();

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
	 * @return current time with possible error delay
	 */
	public static Instant getCurrentTimeMinusError() {
		return getCurrentTime().minus(TIME_ERROR, ChronoUnit.MINUTES);
	}

	/**
	 * @return current time
	 */
	public static Instant getCurrentTime() {
		return OffsetDateTime.now(CLOCK).toInstant();
	}

	/**
	 * Method converts number of seconds to milliseconds in simulation time
	 *
	 * @param seconds number of seconds
	 * @return time in milliseconds
	 */
	public static long convertToSimulationTime(final long seconds) {
		return (long) (((double) seconds / HOUR_DIVIDER) * SECONDS_FOR_HOUR * MILLISECOND_MULTIPLIER);
	}

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
	 * Method computes difference in hours between two dates
	 *
	 * @param startTime time interval start time
	 * @param endTime   time interval end time
	 * @return time in hours
	 */
	public static double differenceInHours(final Instant startTime, final Instant endTime) {
		return (double) SECONDS.between(startTime, endTime) / HOUR_DIVIDER;
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
	 * Method used in testing scenarios to set current time
	 *
	 * @param instant time which is to be set as a current time
	 * @param zone    time zone for current time
	 */
	public static void useMockTime(Instant instant, ZoneId zone) {
		CLOCK = Clock.fixed(instant, zone);
		TimeZone.setDefault(TimeZone.getTimeZone(zone));
	}
}
