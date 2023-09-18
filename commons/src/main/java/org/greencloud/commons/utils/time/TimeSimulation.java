package org.greencloud.commons.utils.time;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.greencloud.commons.constants.TimeConstants;
import org.greencloud.commons.domain.location.Location;
import org.shredzone.commons.suncalc.SunTimes;

/**
 * Class with methods describing current time on which system operates
 */
public class TimeSimulation {
	private static final Clock CLOCK = Clock.systemDefaultZone();
	public static Instant SYSTEM_START_TIME;

	/**
	 * @return current time
	 */
	public static Instant getCurrentTime() {
		return now(CLOCK).toInstant();
	}

	/**
	 * @return current time with possible error delay
	 */
	public static Instant getCurrentTimeMinusError() {
		return getCurrentTime().minus(TimeConstants.TIME_ERROR, MINUTES);
	}

	/**
	 * Method returns sun set and sun rise times
	 *
	 * @param dateTime zone used in retrieving sun time stamps
	 * @param location location for which data is retrieved
	 * @return SunTimes
	 */
	public static SunTimes getSunTimes(final ZonedDateTime dateTime, final Location location) {
		return SunTimes.compute().on(dateTime).at(location.getLatitude(), location.getLongitude()).execute();
	}

	/**
	 * Method sets the system start time to given value
	 */
	public static void setSystemStartTime(Instant instant) {
		SYSTEM_START_TIME = instant;
	}
}
