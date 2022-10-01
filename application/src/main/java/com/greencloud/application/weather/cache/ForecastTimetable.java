package com.greencloud.application.weather.cache;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Comparator.comparingLong;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.weather.domain.AbstractWeather;
import com.greencloud.application.weather.domain.CurrentWeather;
import com.greencloud.application.weather.domain.Forecast;

/**
 * Class represents table of forecast's data mapped to specific time intervals
 */
public class ForecastTimetable {

	private static final Logger logger = LoggerFactory.getLogger(ForecastTimetable.class);
	private static final Duration EXPIRATION_TIME = Duration.of(4, HOURS);

	private final Map<Instant, AbstractWeather> timetable;

	/**
	 * Constructor puts forecast into timetable
	 *
	 * @param forecast com.greencloud.application.weather forecast
	 */
	public ForecastTimetable(Forecast forecast) {
		timetable = new TreeMap<>();
		forecast.getList().forEach(weather -> timetable.put(weather.getTimestamp(), weather));
	}

	/**
	 * Constructor puts current com.greencloud.application.weather into timetable
	 *
	 * @param weather current com.greencloud.application.weather
	 */
	public ForecastTimetable(CurrentWeather weather) {
		timetable = new TreeMap<>();
		timetable.put(weather.getTimestamp(), weather);
	}

	/**
	 * Method updates timetable by putting inside a new forecast
	 *
	 * @param forecast new forecast to be put into timetable
	 */
	public void updateTimetable(Forecast forecast) {
		forecast.getList().forEach(weather -> timetable.put(weather.getTimestamp(), weather));
	}

	/**
	 * Method updates timetable by putting inside current com.greencloud.application.weather
	 *
	 * @param weather current com.greencloud.application.weather to be put into timetable
	 */
	public void updateTimetable(CurrentWeather weather) {
		timetable.put(weather.getTimestamp(), weather);
	}

	/**
	 * Method retrieves forecast from timetable at the time that is closes to the given timestamp
	 *
	 * @param timestamp time for which the com.greencloud.application.weather is to be retrieved
	 * @return com.greencloud.application.weather closest to given time
	 */
	public Optional<AbstractWeather> getFutureWeather(Instant timestamp) {
		var nearestTimestamp = timetable.keySet().stream()
				.min(comparingLong(i -> Math.abs(i.getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));

		if (Duration.between(timestamp, nearestTimestamp).abs().compareTo(EXPIRATION_TIME) > 0) {
			logger.debug("Stale forecast timetable!");
			return Optional.empty();
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
