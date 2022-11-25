package com.greencloud.application.agents.monitoring.management;

import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static java.time.Instant.now;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.monitoring.management.logs.MonitoringManagementLog;
import com.greencloud.application.domain.GreenSourceForecastData;
import com.greencloud.application.domain.GreenSourceWeatherData;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.ImmutableWeatherData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.WeatherData;
import com.greencloud.application.exception.APIFetchInternalException;
import com.greencloud.application.weather.api.OpenWeatherMapApi;
import com.greencloud.application.weather.cache.WeatherCache;
import com.greencloud.application.weather.domain.AbstractWeather;
import com.greencloud.application.weather.domain.FutureWeather;
import com.greencloud.commons.location.Location;

/**
 * Set of methods used in weather management
 */
public class MonitoringWeatherManagement {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringWeatherManagement.class);
	private final OpenWeatherMapApi api;
	private final WeatherCache cache;

	/**
	 * Default constructor
	 */
	public MonitoringWeatherManagement() {
		this.api = new OpenWeatherMapApi();
		this.cache = WeatherCache.getInstance();
	}

	/**
	 * Constructor initializing api and cache for weather
	 *
	 * @param api   weather api
	 * @param cache weather cache
	 */
	public MonitoringWeatherManagement(final OpenWeatherMapApi api, final WeatherCache cache) {
		this.api = api;
		this.cache = cache;
	}

	/**
	 * Method retrieves weather for given location at current moment
	 *
	 * @param requestData information about location
	 * @return weather for current moment
	 */
	public MonitoringData getWeather(GreenSourceWeatherData requestData) {
		logger.info(MonitoringManagementLog.RETRIEVE_WEATHER_LOG, requestData.getLocation());
		return ImmutableMonitoringData.builder()
				.addWeatherData(getWeatherData(requestData.getLocation(), convertToRealTime(now())))
				.build();
	}

	/**
	 * Method retrieves weather for given location at given time
	 *
	 * @param requestData information about location and time
	 * @return weather for given time
	 */
	public MonitoringData getForecast(GreenSourceForecastData requestData) {
		var location = requestData.getLocation();
		logger.info(MonitoringManagementLog.RETRIEVE_WEATHER_LOG, location);
		var weatherData = requestData.getTimetable().stream()
				.map(time -> getForecastData(location, time))
				.toList();
		return ImmutableMonitoringData.builder()
				.weatherData(weatherData)
				.build();
	}

	private WeatherData getWeatherData(Location location, Instant time) {
		return cache.getForecast(location, time).map(f -> buildWeatherData(f, time)).orElseGet(() -> {
					var weather = api.getWeather(location);
					if (nonNull(weather)) {
						cache.updateCache(location, weather);
						return buildWeatherData(weather, weather.getTimestamp());
					} else {
						throw new APIFetchInternalException();
					}
				}
		);
	}

	private WeatherData getForecastData(Location location, Instant time) {
		return cache.getForecast(location, time).map(f -> buildWeatherData(f, time)).orElseGet(() -> {
					var forecast = api.getForecast(location);
					if (nonNull(forecast)) {
						cache.updateCache(location, forecast);
						return buildWeatherData(getNearestForecast(forecast.getList(), time), time);
					} else {
						throw new APIFetchInternalException();
					}
				}
		);
	}

	private WeatherData buildWeatherData(AbstractWeather weather, Instant timestamp) {
		return ImmutableWeatherData.builder()
				.temperature(weather.getMain().getTemp())
				.cloudCover(weather.getClouds().getAll())
				.windSpeed(weather.getWind().getSpeed())
				.time(timestamp)
				.build();
	}

	private FutureWeather getNearestForecast(List<FutureWeather> forecasts, Instant timestamp) {
		return forecasts.stream()
				.min(comparingLong(i -> Math.abs(i.getTimestamp().getEpochSecond() - timestamp.getEpochSecond())))
				.orElseThrow(() -> new NoSuchElementException("No value present"));
	}
}
