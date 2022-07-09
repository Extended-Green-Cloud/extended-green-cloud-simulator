package common;

import exception.IncorrectTaskDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service used to perform operations on date and time structures.
 */
public class TimeUtils {

    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Long TIME_ERROR = 10L;
    private static final int MILLISECOND_MULTIPLIER = 1000;
    private static final int HOUR_DIVIDER = 3600;

    public static final int SECONDS_FOR_HOUR = 3;

    /**
     * Mapper used to convert the date written as string to the offset date
     *
     * @param date string date to be converted to offset date type
     * @return OffsetDateTime date
     */
    public static OffsetDateTime convertToOffsetDateTime(final String date) {
        try {
            final LocalDateTime datetime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
            final ZonedDateTime zoned = datetime.atZone(ZoneId.of("UTC"));
            return zoned.toOffsetDateTime();
        } catch (DateTimeParseException e) {
            logger.info("The provided date format is incorrect");
            e.printStackTrace();
            throw new IncorrectTaskDateException();
        }
    }

    /**
     * @return current time with possible error delay
     */
    public static OffsetDateTime getCurrentTimeMinusError() {
        return getCurrentTime().minusMinutes(TIME_ERROR);
    }

    /**
     * @return current time
     */
    public static OffsetDateTime getCurrentTime() {
        return OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
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
    public static boolean isWithinTimeStamp(final OffsetDateTime timeStampStart,
                                            final OffsetDateTime timeStampEnd,
                                            final OffsetDateTime timeToCheck) {
        return (timeToCheck.isAfter(timeStampStart) && timeToCheck.isBefore(timeStampEnd))
                || timeToCheck.isEqual(timeStampStart) || timeToCheck.isEqual(timeStampEnd);
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
        return (timeToCheck.isAfter(timeStampStart) && timeToCheck.isBefore(timeStampEnd))
                || timeToCheck.equals(timeStampStart) || timeToCheck.equals(timeStampEnd);
    }
}
