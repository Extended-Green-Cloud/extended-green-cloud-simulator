package org.greencloud.weatherapi.mapper;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.List;

import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.weather.ImmutableWeatherData;
import org.greencloud.commons.domain.weather.WeatherData;
import org.greencloud.weatherapi.domain.AbstractWeather;
import org.greencloud.weatherapi.domain.Clouds;
import org.greencloud.weatherapi.domain.CurrentWeather;
import org.greencloud.weatherapi.domain.Forecast;
import org.greencloud.weatherapi.domain.FutureWeather;
import org.greencloud.weatherapi.domain.ImmutableCoord;
import org.greencloud.weatherapi.domain.ImmutableCurrentWeather;
import org.greencloud.weatherapi.domain.ImmutableForecast;
import org.greencloud.weatherapi.domain.ImmutableFutureWeather;
import org.greencloud.weatherapi.domain.ImmutableLocalWeatherDataTimeSeries;
import org.greencloud.weatherapi.domain.ImmutableMain;
import org.greencloud.weatherapi.domain.ImmutableWind;
import org.greencloud.weatherapi.domain.LocalWeatherConfig;
import org.greencloud.weatherapi.domain.LocalWeatherData;
import org.greencloud.weatherapi.domain.LocalWeatherDataTimeSeries;

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
		final double pressureInPa = ofNullable(weather.getMain().getPressure()).orElse(1021D) * 100;
		final double airDensity = pressureInPa / (GAS_CONSTANT * tempInKelvin);
		final Double cloudCover = ofNullable(weather.getClouds()).map(Clouds::getAll).orElse(null);

		return ImmutableWeatherData.builder()
				.temperature(weather.getMain().getTemp())
				.radiation(weather.getMain().getRadiation())
				.windSpeed(weather.getWind().getSpeed())
				.airDensity(airDensity)
				.cloudCover(cloudCover)
				.time(timestamp)
				.build();
	}

	/**
	 * @param localWeatherData data obtained from offline weather data set
	 * @param location         location for which the weather data is provided
	 * @return CurrentWeather
	 */
	public static CurrentWeather mapToCurrentWeather(final LocalWeatherData localWeatherData, final Location location) {
		return ImmutableCurrentWeather.builder()
				.timestamp(getCurrentTime())
				.main(ImmutableMain.builder()
						.temp(localWeatherData.getTemperature())
						.radiation(localWeatherData.getRadiation())
						.build())
				.wind(ImmutableWind.builder()
						.speed(localWeatherData.getWindSpeed())
						.build())
				.coord(ImmutableCoord.builder()
						.lat(location.getLatitude())
						.lon(location.getLongitude())
						.build())
				.build();
	}

	/**
	 * @param localWeatherData data obtained from offline weather data set
	 * @param timestamp        time instant for which the weather data is provided
	 * @return FutureWeather
	 */
	public static FutureWeather mapToFutureWeather(final LocalWeatherData localWeatherData, final Instant timestamp) {
		return ImmutableFutureWeather.builder()
				.main(ImmutableMain.builder()
						.temp(localWeatherData.getTemperature())
						.radiation(localWeatherData.getRadiation())
						.build())
				.wind(ImmutableWind.builder()
						.speed(localWeatherData.getWindSpeed())
						.build())
				.timestamp(timestamp)
				.build();
	}

	/**
	 * @param futureWeatherList list of future weather data
	 * @return Forecast
	 */
	public static Forecast mapToForecast(final List<FutureWeather> futureWeatherList) {
		return ImmutableForecast.builder()
				.cnt(futureWeatherList.size())
				.list(futureWeatherList)
				.build();
	}

	/**
	 * @param weatherData weather data without relevance time
	 * @param config      configuration of weather local data containing weather relevance time
	 * @return LocalWeatherDataTimeSeries
	 */
	public static LocalWeatherDataTimeSeries mapToLocalWeatherDataTimeSeries(
			final LocalWeatherDataTimeSeries weatherData, final LocalWeatherConfig config) {
		return ImmutableLocalWeatherDataTimeSeries.builder()
				.weatherTimeSeries(weatherData.getWeatherTimeSeries())
				.weatherRelevanceMinutes(config.getWeatherRelevanceMinutes())
				.build();
	}
}
