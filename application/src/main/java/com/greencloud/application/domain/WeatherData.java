package com.greencloud.application.domain;

import java.time.Instant;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



@JsonSerialize(as = ImmutableWeatherData.class)
@JsonDeserialize(as = ImmutableWeatherData.class)
@Immutable
public interface WeatherData {

	/**
	 * @return time for when the com.greencloud.application.weather data is valid
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
