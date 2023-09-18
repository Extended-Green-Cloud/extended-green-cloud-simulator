package org.greencloud.commons.domain.weather;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

@JsonSerialize(as = ImmutableWeatherData.class)
@JsonDeserialize(as = ImmutableWeatherData.class)
@Value.Immutable
@ImmutableConfig
public interface WeatherData {

	/**
	 * @return time for when the weather data is valid
	 */
	Instant getTime();

	/**
	 * @return temperature at given location for given time period
	 */
	Double getTemperature();

	/**
	 * @return wind speed at given location for given time period
	 */
	Double getWindSpeed();

	/**
	 * @return cloudiness at given location for given time period
	 */
	Double getCloudCover();

}
