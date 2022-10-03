package com.greencloud.application.agents.monitoring.domain;

import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;

import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.ImmutableWeatherData;
import com.greencloud.application.domain.MonitoringData;

/**
 * Class stores all predefined constants for Monitoring Agent
 *
 * <p> OFFLINE_MODE 		- flag indicating if the monitoring agent should use API or stub data </p>
 * <p> BAD_STUB_PROBABILITY - probability for stubbing data insufficient for job execution </p>
 * <p> STUB_DATA 			- predefined weather data used instead of real API response </p>
 * <p> BAD_STUB_DATA 		- predefined weather data that is insufficient for job execution </p>
 */
public class MonitoringAgentConstants {

	public static final boolean OFFLINE_MODE = true;
	public static final double BAD_STUB_PROBABILITY = 0.1;
	public static final MonitoringData STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(25.0)
							.temperature(25.0)
							.windSpeed(10.0)
							.time(Instant.now())
							.build())
					.build();
	public static final MonitoringData BAD_STUB_DATA =
			ImmutableMonitoringData.builder()
					.addWeatherData(ImmutableWeatherData.builder()
							.cloudCover(50.0)
							.temperature(10.0)
							.windSpeed(5.0)
							.time(getCurrentTime())
							.build())
					.build();
}
