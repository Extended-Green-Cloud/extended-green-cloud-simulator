package config.constants;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import domain.ImmutableWeatherData;
import domain.WeatherData;
import domain.location.ImmutableLocation;
import domain.location.Location;
import weather.domain.Clouds;
import weather.domain.Coord;
import weather.domain.CurrentWeather;
import weather.domain.Forecast;
import weather.domain.FutureWeather;
import weather.domain.ImmutableClouds;
import weather.domain.ImmutableCoord;
import weather.domain.ImmutableCurrentWeather;
import weather.domain.ImmutableForecast;
import weather.domain.ImmutableFutureWeather;
import weather.domain.ImmutableMain;
import weather.domain.ImmutableWeather;
import weather.domain.ImmutableWind;
import weather.domain.Main;
import weather.domain.Weather;
import weather.domain.Wind;

/**
 * Class stores constants used in testing cache methods and classes
 */
public class CacheTestConstants {

	// MOCK OBJECTS
	public static final Location MOCK_LOCATION = ImmutableLocation.builder().latitude(10.0).longitude(15.0).build();
	public static final Instant MOCK_TIME = Instant.parse("2022-01-01T11:00:00.000Z");
	public static final int MOCK_CNT = 5;
	public static final Coord MOCK_COORD = ImmutableCoord.builder().lat(10.0).lon(40.0).build();
	public static final Wind MOCK_WIND = ImmutableWind.builder().speed(10.0).deg(15.0).gust(10.0).build();
	public static final Clouds MOCK_CLOUD = ImmutableClouds.builder().all(5.5).build();
	public static final Main MOCK_MAIN = ImmutableMain.builder()
			.feelsLike(15.0)
			.temp(10.0)
			.minimumTemperature(3.0)
			.maximumTemperature(20.0)
			.pressure(11.5)
			.humidity(0.7)
			.build();

	public static final CurrentWeather MOCK_CURRENT_WEATHER = ImmutableCurrentWeather.builder()
			.clouds(MOCK_CLOUD)
			.weather(prepareMockWeatherList())
			.main(MOCK_MAIN)
			.wind(MOCK_WIND)
			.coord(MOCK_COORD)
			.timestamp(MOCK_TIME)
			.visibility(50.0)
			.timezone(2L)
			.build();

	public static final FutureWeather MOCK_FUTURE_WEATHER = ImmutableFutureWeather.builder()
			.weather(prepareMockWeatherList())
			.clouds(MOCK_CLOUD)
			.wind(MOCK_WIND)
			.main(MOCK_MAIN)
			.visibility(10D)
			.timestamp(MOCK_TIME)
			.build();

	public static final WeatherData MOCK_WEATHER = ImmutableWeatherData.builder()
			.cloudCover(MOCK_CLOUD.getAll())
			.temperature(MOCK_MAIN.getTemp())
			.time(MOCK_TIME)
			.windSpeed(MOCK_WIND.getSpeed())
			.build();

	public static final Forecast MOCK_FORECAST = ImmutableForecast.builder()
			.cnt(MOCK_CNT)
			.list(List.of(MOCK_FUTURE_WEATHER))
			.build();

	private static List<Weather> prepareMockWeatherList() {
		final List<Weather> mockWeatherList = new ArrayList<>();
		for (long i = 1; i <= 5; i++) {
			final Weather weather = ImmutableWeather.builder()
					.id(i)
					.main(String.format("Main%d", i))
					.description(String.format("Description%d", i))
					.icon(String.format("Icon%d", i))
					.build();
			mockWeatherList.add(weather);
		}
		return mockWeatherList;
	}
}
