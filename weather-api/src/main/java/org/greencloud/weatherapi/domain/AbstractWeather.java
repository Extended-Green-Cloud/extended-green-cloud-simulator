package org.greencloud.weatherapi.domain;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface AbstractWeather {

	Main getMain();

	Wind getWind();

	@Nullable
	Clouds getClouds();

}
