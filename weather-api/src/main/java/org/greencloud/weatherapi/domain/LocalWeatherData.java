package org.greencloud.weatherapi.domain;

import org.greencloud.commons.domain.location.Location;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableLocalWeatherData.class)
@JsonDeserialize(as = ImmutableLocalWeatherData.class)
@Value.Immutable
public interface LocalWeatherData {

	@JsonProperty("temp")
	Double getTemperature();

	Double getRadiation();

	@JsonProperty("wind_speed")
	Double getWindSpeed();
}
