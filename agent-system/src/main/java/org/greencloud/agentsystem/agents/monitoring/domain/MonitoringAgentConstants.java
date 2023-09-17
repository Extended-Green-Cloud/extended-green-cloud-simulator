package org.greencloud.agentsystem.agents.monitoring.domain;

import static com.greencloud.commons.time.TimeUtils.getCurrentTime;

import com.greencloud.commons.domain.weather.ImmutableMonitoringData;
import com.greencloud.commons.domain.weather.ImmutableWeatherData;
import com.greencloud.commons.domain.weather.MonitoringData;

/**
 * Class stores all predefined constants for Monitoring Agent
 *
 * <p> BAD_STUB_PROBABILITY 		  - probability for stubbing data insufficient for job execution </p>
 * <p> STUB_DATA 					  - predefined weather data used instead of real API response </p>
 * <p> BAD_STUB_DATA 				  - predefined weather data that is insufficient for job execution </p>
 * <p> MAX_NUMBER_OF_WEATHER_REQUESTS - maximal number of messages asking for weather data that can be read at once </p>
 * <p> WEATHER_REQUESTS_IN_BATCH      - maximal number of weather requests processed in batch </p>
 */
public class MonitoringAgentConstants {

	public static final MonitoringData STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(25.0)
							.temperature(25.0)
							.windSpeed(10.0)
							.time(getCurrentTime())
							.build())
					.build();
	public static final MonitoringData BAD_STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(0.0)
							.temperature(0.0)
							.windSpeed(0.0)
							.time(getCurrentTime())
							.build())
					.build();
	public static final int MAX_NUMBER_OF_WEATHER_REQUESTS = 100;
	public static final int WEATHER_REQUESTS_IN_BATCH = 10;
}
