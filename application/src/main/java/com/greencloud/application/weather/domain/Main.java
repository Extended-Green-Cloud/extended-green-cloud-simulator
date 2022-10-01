package com.greencloud.application.weather.domain;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableMain.class)
@JsonDeserialize(as = ImmutableMain.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Immutable
public interface Main {

	Double getTemp();

	@JsonProperty("feels_like")
	Double getFeelsLike();

	@JsonProperty("temp_min")
	Double getMinimumTemperature();

	@JsonProperty("temp_max")
	Double getMaximumTemperature();

	Double getPressure();

	Double getHumidity();

	@JsonProperty("sea_level")
	@Nullable
	Double getSeaLeve();

	@JsonProperty("grnd_level")
	@Nullable
	Double getGroundLever();
}
