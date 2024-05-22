package org.greencloud.agentsystem.agents.monitoring.domain;

import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import org.greencloud.commons.domain.weather.ImmutableMonitoringData;
import org.greencloud.commons.domain.weather.ImmutableWeatherData;
import org.greencloud.commons.domain.weather.MonitoringData;

/**
 * Class stores all predefined constants for Monitoring Agent.
 */
public class MonitoringAgentConstants {

	/**
	 * Predefined weather data used instead of real API response.
	 */
	public static final MonitoringData STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(25.0)
							.temperature(25.0)
							.windSpeed(10.0)
							.airDensity(0.31)
							.time(getCurrentTime())
							.build())
					.build();
	/**
	 * Predefined weather data that is insufficient for job execution.
	 */
	public static final MonitoringData BAD_STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(0.0)
							.temperature(0.0)
							.windSpeed(0.0)
							.airDensity(0.31)
							.time(getCurrentTime())
							.build())
					.build();
	/**
	 * Maximal number of messages asking for the weather data that can be read at once.
	 */
	public static final int MAX_NUMBER_OF_REQUESTS = 100;
	/**
	 * Maximal number of weather requests processed in batch.
	 */
	public static final int WEATHER_REQUESTS_IN_BATCH = 10;
}
