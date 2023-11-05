package org.greencloud.weatherapi.mapper;

import java.time.Instant;

import org.greencloud.weatherapi.domain.AbstractWeather;

import org.greencloud.commons.domain.weather.ImmutableWeatherData;
import org.greencloud.commons.domain.weather.WeatherData;

/**
 * Class provides set of methods mapping weather object classes
 */
public class WeatherMapper {

	private static final Double GAS_CONSTANT = 287.05;

	/**
	 * @param weather   abstract weather data
	 * @param timestamp time instant
	 * @return WeatherData
	 */
	public static WeatherData mapToWeatherData(final AbstractWeather weather, final Instant timestamp) {
		final double tempInKelvin = weather.getMain().getTemp() + 273.15;
		final double pressureInPa = weather.getMain().getPressure() * 100;
		final double airDensity = pressureInPa / (GAS_CONSTANT * tempInKelvin);

		return ImmutableWeatherData.builder()
				.temperature(weather.getMain().getTemp())
				.cloudCover(weather.getClouds().getAll())
				.airDensity(airDensity)
				.windSpeed(weather.getWind().getSpeed())
				.time(timestamp)
				.build();
	}
}
