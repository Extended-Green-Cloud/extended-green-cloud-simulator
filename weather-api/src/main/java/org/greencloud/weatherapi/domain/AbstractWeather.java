package org.greencloud.weatherapi.domain;

public interface AbstractWeather {

	Main getMain();

	Wind getWind();

	Clouds getClouds();

}
