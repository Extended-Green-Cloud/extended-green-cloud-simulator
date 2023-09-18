package org.greencloud.weatherapi.cache;

import static java.time.Duration.between;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Comparator.comparingLong;
import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

import org.greencloud.weatherapi.domain.AbstractWeather;
import org.greencloud.weatherapi.domain.CurrentWeather;
import org.greencloud.weatherapi.domain.Forecast;
import org.slf4j.Logger;

/**
 * Class represents table of forecast's data mapped to specific time intervals
 */
public class ForecastTimetable {

	private static final Logger logger = getLogger(ForecastTimetable.class);
	private static final Duration EXPIRATION_TIME = Duration.of(4, HOURS);

	private final Map<Instant, AbstractWeather> timetable;

	/**
	 * Constructor puts forecast into timetable
	 *
	 * @param forecast weather forecast
	 */
	public ForecastTimetable(Forecast forecast) {
		timetable = new TreeMap<>();
		forecast.getList().forEach(weather -> timetable.put(weather.getTimestamp(), weather));
	}

	/**
	 * Constructor puts current weather into timetable
	 *
	 * @param weather current weather
	 */
	public ForecastTimetable(CurrentWeather weather) {
		timetable = new TreeMap<>();
		timetable.put(weather.getTimestamp(), weather);
	}

	/**
	 * Method updates timetable by putting a new forecast inside it
	 *
	 * @param forecast new forecast to be put into timetable
	 */
	public void updateTimetable(Forecast forecast) {
		forecast.getList().forEach(weather -> timetable.put(weather.getTimestamp(), weather));
	}

	/**
	 * Method updates timetable by putting inside current weather
	 *
	 * @param weather current weather to be put into timetable
	 */
	public void updateTimetable(CurrentWeather weather) {
		timetable.put(weather.getTimestamp(), weather);
	}

	/**
	 * Method retrieves forecast from timetable at the time that is closes to the given timestamp
	 *
	 * @param timestamp time for which the weather is to be retrieved
	 * @return weather closest to given time
	 */
	public Optional<AbstractWeather> getFutureWeather(Instant timestamp) {
		final Instant nearestTimestamp = timetable.keySet().stream()
				.min(comparingLong(i -> Math.abs(i.getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));

		if (between(timestamp, nearestTimestamp).abs().compareTo(EXPIRATION_TIME) > 0) {
			logger.debug("Stale forecast timetable!");
			return empty();
		}

		return Optional.of(timetable.get(nearestTimestamp));
	}

	/**
	 * @return forecast timetable
	 */
	public Map<Instant, AbstractWeather> getTimetable() {
		return timetable;
	}
}
