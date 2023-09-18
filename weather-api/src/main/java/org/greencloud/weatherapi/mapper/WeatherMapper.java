package org.greencloud.weatherapi.mapper;

import java.time.Instant;

import org.greencloud.weatherapi.domain.AbstractWeather;

import org.greencloud.commons.domain.weather.ImmutableWeatherData;
import org.greencloud.commons.domain.weather.WeatherData;

/**
 * Class provides set of methods mapping weather object classes
 */
public class WeatherMapper {

	/**
	 * @param weather   abstract weather data
	 * @param timestamp time instant
	 * @return WeatherData
	 */
	public static WeatherData mapToWeatherData(final AbstractWeather weather, final Instant timestamp) {
		return ImmutableWeatherData.builder()
				.temperature(weather.getMain().getTemp())
				.cloudCover(weather.getClouds().getAll())
				.windSpeed(weather.getWind().getSpeed())
				.time(timestamp)
				.build();
	}
}
