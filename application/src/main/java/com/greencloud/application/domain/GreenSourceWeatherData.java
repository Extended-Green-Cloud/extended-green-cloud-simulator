package com.greencloud.application.domain;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.application.domain.location.Location;

/**
 * Object storing the data passed by the Green Source in the com.greencloud.application.weather query message
 */
@JsonSerialize(as = ImmutableGreenSourceWeatherData.class)
@JsonDeserialize(as = ImmutableGreenSourceWeatherData.class)
@Immutable
public interface GreenSourceWeatherData {

	/**
	 * @return location for which the com.greencloud.application.weather is to be retrieved
	 */
	Location getLocation();
}
