package agents.monitoring;

import static config.constants.CacheTestConstants.MOCK_CURRENT_WEATHER;
import static config.constants.CacheTestConstants.MOCK_FORECAST;
import static config.constants.CacheTestConstants.MOCK_FUTURE_WEATHER;
import static config.constants.CacheTestConstants.MOCK_LOCATION;
import static config.constants.CacheTestConstants.MOCK_TIME;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import agents.monitoring.management.MonitoringWeatherManagement;
import domain.GreenSourceForecastData;
import domain.GreenSourceWeatherData;
import domain.ImmutableGreenSourceForecastData;
import domain.ImmutableGreenSourceWeatherData;
import domain.MonitoringData;
import exception.APIFetchInternalException;
import weather.api.OpenWeatherMapApi;
import weather.cache.WeatherCache;
import weather.domain.Clouds;
import weather.domain.CurrentWeather;
import weather.domain.Forecast;
import weather.domain.FutureWeather;
import weather.domain.ImmutableClouds;
import weather.domain.ImmutableCurrentWeather;
import weather.domain.ImmutableForecast;
import weather.domain.ImmutableFutureWeather;
import weather.domain.ImmutableWind;
import weather.domain.Wind;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class MonitoringWeatherManagementUnitTest {

	// MOCKED OBJECTS
	public static final GreenSourceWeatherData MOCK_GS_WEATHER = ImmutableGreenSourceWeatherData.builder()
			.location(MOCK_LOCATION)
			.build();
	public static final GreenSourceForecastData MOCK_GS_FORECAST = ImmutableGreenSourceForecastData.builder()
			.location(MOCK_LOCATION)
			.addTimetable(MOCK_TIME)
			.build();

	@Mock
	private OpenWeatherMapApi mockAPI;
	private WeatherCache mockCache;

	private MonitoringWeatherManagement monitoringWeatherManagement;

	@BeforeEach
	void init() {
		mockCache = WeatherCache.getInstance();
		prepareCache();
		monitoringWeatherManagement = new MonitoringWeatherManagement(mockAPI, mockCache);
	}

	@Test
	@DisplayName("Test getting current weather present in cache")
	void testGetWeatherPresentInCache() {
		final Forecast currentForecast = ImmutableForecast.copyOf(MOCK_FORECAST)
				.withList(ImmutableFutureWeather.copyOf(MOCK_FUTURE_WEATHER).withTimestamp(
						now()));
		mockCache.updateCache(MOCK_LOCATION, currentForecast);

		final MonitoringData result = monitoringWeatherManagement.getWeather(MOCK_GS_WEATHER);

		assertThat(result.getWeatherData()).hasSize(1);
		assertThat(result.getWeatherData().get(0).getCloudCover()).isEqualTo(5.5);
	}

	@Test
	@DisplayName("Test getting current weather for api call")
	void testGetWeatherApiCall() {
		final Clouds newClouds = ImmutableClouds.builder().all(200.0).build();
		final CurrentWeather currentWeather = ImmutableCurrentWeather.copyOf(MOCK_CURRENT_WEATHER)
				.withClouds(newClouds)
				.withTimestamp(now());
		doReturn(currentWeather).when(mockAPI).getWeather(MOCK_LOCATION);
		assertThat(mockCache.getForecast(MOCK_LOCATION, now())).isEmpty();

		final MonitoringData result = monitoringWeatherManagement.getWeather(MOCK_GS_WEATHER);

		// Validate return

		assertThat(result.getWeatherData()).hasSize(1);
		assertThat(result.getWeatherData().get(0).getCloudCover()).isEqualTo(200.0);
		assertThat(result.getWeatherData().get(0).getTemperature()).isEqualTo(10.0);

		// Validate cache update

		assertThat(mockCache.getForecast(MOCK_LOCATION, now())).isPresent();
		assertThat(mockCache.getForecast(MOCK_LOCATION, now()).get().getClouds()).isEqualTo(newClouds);
	}

	@Test
	@DisplayName("Test getting current weather for api call not present")
	void testGetWeatherApiCallNotPresent() {
		doReturn(null).when(mockAPI).getWeather(MOCK_LOCATION);

		assertThatThrownBy(() -> monitoringWeatherManagement.getWeather(MOCK_GS_WEATHER))
				.isInstanceOf(APIFetchInternalException.class)
				.hasMessage("The API retrieved null instead of the weather data");
	}

	@Test
	@DisplayName("Test getting forecast for location present")
	void testGetForecastLocationPresent() {
		assertThat(mockCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isPresent();
		final MonitoringData result = monitoringWeatherManagement.getForecast(MOCK_GS_FORECAST);

		assertThat(result.getWeatherData()).hasSize(1);
		assertThat(result.getWeatherData().get(0).getWindSpeed()).isEqualTo(10.0);
		assertThat(result.getWeatherData().get(0).getCloudCover()).isEqualTo(5.5);
	}

	@Test
	@DisplayName("Test getting forecast for api call one forecast retrieved")
	void testGetForecastApiCallOneForecast() {
		final Instant newTime = Instant.parse("2022-10-01T11:00:00.000Z");
		final Wind newWind = ImmutableWind.builder()
				.speed(100.0)
				.deg(10.0)
				.gust(5.0)
				.build();
		final Forecast forecastWeather = ImmutableForecast.copyOf(MOCK_FORECAST)
				.withList(ImmutableFutureWeather.copyOf(MOCK_FUTURE_WEATHER)
						.withTimestamp(newTime)
						.withWind(newWind));
		final GreenSourceForecastData newGSForecast = ImmutableGreenSourceForecastData.copyOf(MOCK_GS_FORECAST)
						.withTimetable(newTime);
		doReturn(forecastWeather).when(mockAPI).getForecast(MOCK_LOCATION);

		assertThat(mockCache.getForecast(MOCK_LOCATION, newTime)).isEmpty();

		final MonitoringData result = monitoringWeatherManagement.getForecast(newGSForecast);

		// Validate return

		assertThat(result.getWeatherData()).hasSize(1);
		assertThat(result.getWeatherData().get(0).getWindSpeed()).isEqualTo(100.0);
		assertThat(result.getWeatherData().get(0).getCloudCover()).isEqualTo(5.5);

		// Validate cache update

		assertThat(mockCache.getForecast(MOCK_LOCATION, newTime)).isPresent();
		assertThat(mockCache.getForecast(MOCK_LOCATION, newTime).get().getWind()).isEqualTo(newWind);
	}

	@Test
	@DisplayName("Test getting forecast for api call one many forecasts retrieved")
	void testGetForecastApiCallManyForecasts() {
		final Instant newTime = Instant.parse("2022-10-01T11:00:00.000Z");
		final Instant time1 = Instant.parse("2022-10-01T10:00:00.000Z");
		final Instant time2 = Instant.parse("2022-10-01T13:00:00.000Z");
		final FutureWeather futureWeather1 = ImmutableFutureWeather.copyOf(MOCK_FUTURE_WEATHER)
				.withTimestamp(time1)
				.withClouds(ImmutableClouds.builder().all(100.0).build());
		final FutureWeather futureWeather2 = ImmutableFutureWeather.copyOf(MOCK_FUTURE_WEATHER)
				.withTimestamp(time2)
				.withClouds(ImmutableClouds.builder().all(200.0).build());
		final Forecast forecastWeather = ImmutableForecast.copyOf(MOCK_FORECAST)
				.withList(futureWeather1, futureWeather2);
		final GreenSourceForecastData newGSForecast = ImmutableGreenSourceForecastData.copyOf(MOCK_GS_FORECAST)
				.withTimetable(newTime);
		doReturn(forecastWeather).when(mockAPI).getForecast(MOCK_LOCATION);

		assertThat(mockCache.getForecast(MOCK_LOCATION, newTime)).isEmpty();

		final MonitoringData result = monitoringWeatherManagement.getForecast(newGSForecast);

		// Validate return

		assertThat(result.getWeatherData()).hasSize(1);
		assertThat(result.getWeatherData().get(0).getCloudCover()).isEqualTo(100.0);

		// Validate cache update

		assertThat(mockCache.getForecast(MOCK_LOCATION, time1)).isPresent();
		assertThat(mockCache.getForecast(MOCK_LOCATION, time2)).isPresent();
		assertThat(mockCache.getForecast(MOCK_LOCATION, time1).get().getClouds().getAll()).isEqualTo(100.0);
		assertThat(mockCache.getForecast(MOCK_LOCATION, time2).get().getClouds().getAll()).isEqualTo(200.0);
	}

	@Test
	@DisplayName("Test getting forecast for api call not present")
	void testGetForecastApiCallNotPresent() {
		mockCache.clearCache();
		doReturn(null).when(mockAPI).getForecast(MOCK_LOCATION);

		assertThatThrownBy(() -> monitoringWeatherManagement.getForecast(MOCK_GS_FORECAST))
				.isInstanceOf(APIFetchInternalException.class)
				.hasMessage("The API retrieved null instead of the weather data");
	}

	private void prepareCache() {
		mockCache.clearCache();
		mockCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);
	}

}
