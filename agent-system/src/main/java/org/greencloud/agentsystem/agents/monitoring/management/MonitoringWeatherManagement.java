package org.greencloud.agentsystem.agents.monitoring.management;

import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToWeatherData;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.greencloud.agentsystem.agents.monitoring.management.logs.MonitoringManagementLog;
import org.greencloud.weatherapi.api.OpenWeatherMapApi;
import org.greencloud.weatherapi.cache.WeatherCache;
import org.slf4j.Logger;

import org.greencloud.commons.domain.agent.GreenSourceForecastData;
import org.greencloud.commons.domain.agent.GreenSourceWeatherData;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.weather.ImmutableMonitoringData;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.domain.weather.WeatherData;

/**
 * Set of methods used in weather management
 */
public class MonitoringWeatherManagement implements Serializable {

	private static final Logger logger = getLogger(MonitoringWeatherManagement.class);

	private final transient OpenWeatherMapApi api;
	private final transient WeatherCache cache;

	/**
	 * Default constructor
	 */
	public MonitoringWeatherManagement() {
		this.api = new OpenWeatherMapApi();
		this.cache = WeatherCache.getInstance();
	}

	/**
	 * Method retrieves weather for given location at current moment
	 *
	 * @param data information about location
	 * @return weather for current moment
	 */
	public MonitoringData getWeather(final GreenSourceWeatherData data) {
		logger.info(MonitoringManagementLog.RETRIEVE_WEATHER_LOG, data.getLocation());
		return ImmutableMonitoringData.builder()
				.addWeatherData(getWeatherData(data.getLocation(), convertToRealTime(getCurrentTime())))
				.build();
	}

	/**
	 * Method retrieves weather for given location at given time
	 *
	 * @param requestData information about location and time
	 * @return weather for given time
	 */
	public MonitoringData getForecast(final GreenSourceForecastData requestData) {
		final Location location = requestData.getLocation();
		logger.info(MonitoringManagementLog.RETRIEVE_WEATHER_LOG, location);
		final List<WeatherData> weatherData = requestData.getTimetable().stream()
				.map(time -> getForecastData(location, time))
				.toList();
		return new ImmutableMonitoringData(weatherData);
	}

	private WeatherData getWeatherData(final Location location, final Instant time) {
		return cache.getForecast(location, time)
				.map(forecast -> mapToWeatherData(forecast, time))
				.orElseGet(() -> cache.getCurrentWeatherAndPopulateCache(api, location));
	}

	private WeatherData getForecastData(Location location, Instant time) {
		return cache.getForecast(location, time)
				.map(forecast -> mapToWeatherData(forecast, time))
				.orElseGet(() -> cache.getForecastAndPopulateCache(api, location, time));
	}
}
