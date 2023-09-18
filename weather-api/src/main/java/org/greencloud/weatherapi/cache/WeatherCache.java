package org.greencloud.weatherapi.cache;

import static java.lang.Math.abs;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToWeatherData;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.greencloud.weatherapi.api.OpenWeatherMapApi;
import org.greencloud.weatherapi.domain.AbstractWeather;
import org.greencloud.weatherapi.domain.CurrentWeather;
import org.greencloud.weatherapi.domain.Forecast;
import org.greencloud.weatherapi.domain.FutureWeather;

import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.weather.WeatherData;
import org.greencloud.commons.exception.APIFetchInternalException;

/**
 * Class represents a weather cache storing weather forecast for given location
 */
public class WeatherCache {

	private static final Map<Location, ForecastTimetable> CACHE = new HashMap<>();
	private static final WeatherCache instance = new WeatherCache();

	private WeatherCache() {
	}

	public static WeatherCache getInstance() {
		return instance;
	}

	/**
	 * Method retrieves weather forecast for given location and timestamp
	 *
	 * @param location  location for which the weather is to be retrieved
	 * @param timestamp time for which the weather is to be retrieved
	 * @return AbstractWeather forecast
	 */
	public Optional<AbstractWeather> getForecast(Location location, Instant timestamp) {
		if (CACHE.containsKey(location)) {
			return CACHE.get(location).getFutureWeather(timestamp);
		}
		return empty();
	}

	/**
	 * Method returns weather forecast data and populates with it the cache
	 *
	 * @param api      api used to fetch weather
	 * @param location location for which weather is to be fetched
	 * @param time     time for which the weather is to be fetched
	 * @return weather data
	 */
	public WeatherData getForecastAndPopulateCache(final OpenWeatherMapApi api, final Location location,
			final Instant time) {
		final Forecast forecast = api.getForecast(location);
		if (nonNull(forecast)) {
			updateCache(location, forecast);
			return mapToWeatherData(getNearestForecast(forecast.getList(), time), time);
		} else {
			throw new APIFetchInternalException();
		}
	}

	/**
	 * Method returns current weather data and populates with it the cache
	 *
	 * @param api      api used to fetch weather
	 * @param location location for which weather is to be fetched
	 * @return weather data
	 */
	public WeatherData getCurrentWeatherAndPopulateCache(final OpenWeatherMapApi api, final Location location) {
		final CurrentWeather weather = api.getWeather(location);
		if (nonNull(weather)) {
			updateCache(location, weather);
			return mapToWeatherData(weather, weather.getTimestamp());
		} else {
			throw new APIFetchInternalException();
		}
	}

	/**
	 * Method updates the cache with given forecast
	 *
	 * @param location location for given forecast
	 * @param forecast weather forecast
	 */
	public void updateCache(Location location, Forecast forecast) {
		if (CACHE.containsKey(location)) {
			CACHE.get(location).updateTimetable(forecast);
		} else {
			CACHE.put(location, new ForecastTimetable(forecast));
		}
	}

	/**
	 * Method updates the cache with given current weather
	 *
	 * @param location       location for given weather
	 * @param currentWeather weather
	 */
	public void updateCache(Location location, CurrentWeather currentWeather) {
		if (CACHE.containsKey(location)) {
			CACHE.get(location).updateTimetable(currentWeather);
		} else {
			CACHE.put(location, new ForecastTimetable(currentWeather));
		}
	}

	private FutureWeather getNearestForecast(final List<FutureWeather> forecasts, final Instant timestamp) {
		return forecasts.stream()
				.min(comparingLong(i -> abs(i.getTimestamp().getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));
	}

	/**
	 * Method clears the current cache (for testing purposes)
	 */
	public void clearCache() {
		CACHE.clear();
	}
}
