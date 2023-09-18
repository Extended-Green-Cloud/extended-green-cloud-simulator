package org.greencloud.commons.domain.agent;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.location.Location;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data passed by the Green Source in the weather query message
 */
@JsonSerialize(as = ImmutableGreenSourceWeatherData.class)
@JsonDeserialize(as = ImmutableGreenSourceWeatherData.class)
@Value.Immutable
@ImmutableConfig
public interface GreenSourceWeatherData {

	/**
	 * @return location for which the weather is to be retrieved
	 */
	Location getLocation();

	double getPredictionError();
}
