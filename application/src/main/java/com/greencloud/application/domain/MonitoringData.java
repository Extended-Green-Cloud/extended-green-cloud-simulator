package com.greencloud.application.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object storing the data passed by the Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringData.class)
@JsonDeserialize(as = ImmutableMonitoringData.class)
@Immutable
public interface MonitoringData {

	/**
	 * Serves as com.greencloud.application.weather timetable that is sent from Monitoring Agent to the Green Source Energy Agent
	 *
	 * @return list of {@link MonitoringData}
	 */
	List<WeatherData> getWeatherData();

	default Optional<WeatherData> getDataForTimestamp(Instant timestamp) {
		return getWeatherData().stream().filter(weatherData -> weatherData.getTime().equals(timestamp)).findFirst();
	}
}
