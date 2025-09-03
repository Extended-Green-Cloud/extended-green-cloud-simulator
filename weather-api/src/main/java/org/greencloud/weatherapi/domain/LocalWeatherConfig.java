package org.greencloud.weatherapi.domain;

import org.greencloud.commons.domain.location.Location;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableLocalWeatherConfig.class)
@JsonDeserialize(as = ImmutableLocalWeatherConfig.class)
@Value.Immutable
public interface LocalWeatherConfig {

	/**
	 * @return location for which weather data is specified
	 */
	Location getLocation();

	/**
	 * @return time in minutes for which the weather data is relevant
	 */
	Integer getWeatherRelevanceMinutes();

	/**
	 * @return name of the data source where weather data is located
	 */
	String getDataSource();
}
