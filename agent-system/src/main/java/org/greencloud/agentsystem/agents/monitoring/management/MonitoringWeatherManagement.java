package org.greencloud.agentsystem.agents.monitoring.management;

import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTimeElapsing;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToWeatherData;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.greencloud.agentsystem.agents.monitoring.MonitoringAgent;
import org.greencloud.commons.domain.agent.GreenSourceForecastData;
import org.greencloud.commons.domain.agent.GreenSourceWeatherData;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.weather.ImmutableMonitoringData;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.domain.weather.WeatherData;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.commons.utils.time.TimeConverter;
import org.greencloud.weatherapi.api.OpenWeatherMapApi;
import org.greencloud.weatherapi.cache.WeatherCache;

/**
 * Set of methods used in weather management.
 */
public class MonitoringWeatherManagement implements Serializable {

	private static final LogBuilder log = constructLogBuilder(MonitoringWeatherManagement.class);

	private final transient OpenWeatherMapApi api;
	private final transient WeatherCache cache;

	/**
	 * Default constructor.
	 */
	public MonitoringWeatherManagement(final MonitoringAgent agent) {
		this.api = new OpenWeatherMapApi(agent.getProperties().getConfig().getWeatherFetchingMode());
		this.cache = WeatherCache.getInstance();
	}

	/**
	 * Method retrieves weather for given location at current moment.
	 *
	 * @param data information about location
	 * @return weather for current moment
	 */
	public MonitoringData getWeather(final GreenSourceWeatherData data) {
		log.messageInfo("Retrieving forecast info for {}!", data.getLocation());
		return ImmutableMonitoringData.builder()
				.addWeatherData(getWeatherData(data.getLocation(), convertToRealTimeElapsing(getCurrentTime())))
				.build();
	}

	/**
	 * Method retrieves weather for given location at given time.
	 *
	 * @param requestData information about location and time
	 * @return weather for given time
	 */
	public MonitoringData getForecast(final GreenSourceForecastData requestData) {
		final Location location = requestData.getLocation();
		log.messageInfo("Retrieving forecast info for {}!", location);
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
