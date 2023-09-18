package org.greencloud.commons.domain.weather;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data passed by the Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringData.class)
@JsonDeserialize(as = ImmutableMonitoringData.class)
@Value.Immutable
@ImmutableConfig
public interface MonitoringData {

	/**
	 * Serves as weather timetable that is sent from Monitoring Agent to the Green Source Energy Agent
	 *
	 * @return list of {@link MonitoringData}
	 */
	List<WeatherData> getWeatherData();

	default Optional<WeatherData> getDataForTimestamp(Instant timestamp) {
		return getWeatherData().stream().filter(weatherData -> weatherData.getTime().equals(timestamp)).findFirst();
	}
}
