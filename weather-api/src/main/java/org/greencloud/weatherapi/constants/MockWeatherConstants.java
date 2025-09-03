package org.greencloud.weatherapi.constants;

import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import org.greencloud.weatherapi.domain.CurrentWeather;
import org.greencloud.weatherapi.domain.FutureWeather;
import org.greencloud.weatherapi.domain.ImmutableClouds;
import org.greencloud.weatherapi.domain.ImmutableCoord;
import org.greencloud.weatherapi.domain.ImmutableCurrentWeather;
import org.greencloud.weatherapi.domain.ImmutableFutureWeather;
import org.greencloud.weatherapi.domain.ImmutableMain;
import org.greencloud.weatherapi.domain.ImmutableWind;

public class MockWeatherConstants {

	/**
	 * Predefined weather data that is insufficient for job execution.
	 */
	public static final CurrentWeather CURRENT_WEATHER_BAD_STUB =
			ImmutableCurrentWeather.builder()
					.timestamp(getCurrentTime())
					.main(ImmutableMain.builder()
							.temp(0D)
							.radiation(0D)
							.pressure(900D)
							.build())
					.wind(ImmutableWind.builder()
							.speed(0D)
							.build())
					.clouds(ImmutableClouds.builder()
							.all(0D)
							.build())
					.coord(ImmutableCoord.builder()
							.lat(30D)
							.lon(30D)
							.build())
					.build();

	/**
	 * Predefined weather data used instead of real API response.
	 */
	public static final CurrentWeather CURRENT_WEATHER_STUB =
			ImmutableCurrentWeather.builder()
					.timestamp(getCurrentTime())
					.main(ImmutableMain.builder()
							.temp(25.0)
							.radiation(13.0)
							.pressure(1021D)
							.build())
					.wind(ImmutableWind.builder()
							.speed(10D)
							.build())
					.clouds(ImmutableClouds.builder()
							.all(25D)
							.build())
					.coord(ImmutableCoord.builder()
							.lat(30D)
							.lon(30D)
							.build())
					.build();

	/**
	 * Predefined weather data used instead of real API response.
	 */
	public static final FutureWeather FUTURE_WEATHER_STUB =
			ImmutableFutureWeather.builder()
					.timestamp(getCurrentTime())
					.main(ImmutableMain.builder()
							.temp(25.0)
							.radiation(13.0)
							.pressure(1021D)
							.build())
					.wind(ImmutableWind.builder()
							.speed(10D)
							.build())
					.clouds(ImmutableClouds.builder()
							.all(25D)
							.build())
					.build();
}
