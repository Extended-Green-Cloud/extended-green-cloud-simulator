package com.greencloud.application.weather.cache;

import static com.greencloud.application.constants.CacheTestConstants.MOCK_CLOUD;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_CURRENT_WEATHER;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_FORECAST;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_FUTURE_WEATHER;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_LOCATION;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_MAIN;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_TIME;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_WIND;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.location.ImmutableLocation;
import com.greencloud.application.domain.location.Location;
import com.greencloud.application.weather.domain.AbstractWeather;
import com.greencloud.application.weather.domain.CurrentWeather;
import com.greencloud.application.weather.domain.Forecast;
import com.greencloud.application.weather.domain.FutureWeather;
import com.greencloud.application.weather.domain.ImmutableCurrentWeather;
import com.greencloud.application.weather.domain.ImmutableForecast;
import com.greencloud.application.weather.domain.ImmutableFutureWeather;

class WeatherCacheUnitTest {

	private WeatherCache weatherCache;

	@BeforeEach
	void init() {
		weatherCache = WeatherCache.getInstance();
		weatherCache.clearCache();
	}

	@Test
	@DisplayName("Test getting forecast for empty cache")
	void testGetForecastEmptyCache() {
		assertThat(weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isEmpty();
	}

	@Test
	@DisplayName("Test getting forecast for location not present")
	void testGetForecastLocationNotPresent() {
		final Location absentLocation = ImmutableLocation.builder().latitude(0).longitude(0).build();
		weatherCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);
		assertThat(weatherCache.getForecast(absentLocation, MOCK_TIME)).isEmpty();
	}

	@Test
	@DisplayName("Test getting forecast for timestamp not present")
	void testGetForecastTimestampNotPresent() {
		final Instant newTimeStamp = Instant.parse("2022-10-01T11:00:00.000Z");
		weatherCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);
		assertThat(weatherCache.getForecast(MOCK_LOCATION, newTimeStamp)).isEmpty();
	}

	@Test
	@DisplayName("Test getting forecast for correct location and timestamp")
	void testGetForecastCorrectData() {
		weatherCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);

		final Optional<AbstractWeather> afterUpdate = weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME);

		assertThat(afterUpdate).isPresent();
		assertThat(afterUpdate.get().getClouds()).isEqualTo(MOCK_CLOUD);
		assertThat(afterUpdate.get().getMain()).isEqualTo(MOCK_MAIN);
		assertThat(afterUpdate.get().getWind()).isEqualTo(MOCK_WIND);
	}

	@Test
	@DisplayName("Test update cache with forecast for location not present")
	void testUpdateCacheForecastLocationNotPresent() {
		assertThat(weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isEmpty();
		weatherCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);

		final Optional<AbstractWeather> afterUpdate = weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME);

		assertThat(afterUpdate).isPresent();
		assertThat(afterUpdate.get().getClouds()).isEqualTo(MOCK_CLOUD);
		assertThat(afterUpdate.get().getMain()).isEqualTo(MOCK_MAIN);
	}

	@Test
	@DisplayName("Test update cache with forecast for location present")
	void testUpdateCacheForecastLocationPresent() {
		final Instant newTimeStamp = Instant.parse("2022-10-01T11:00:00.000Z");
		final FutureWeather newWeather = ImmutableFutureWeather.copyOf(MOCK_FUTURE_WEATHER).withTimestamp(newTimeStamp);
		final Forecast newForeCast = ImmutableForecast.copyOf(MOCK_FORECAST).withList(newWeather);

		weatherCache.updateCache(MOCK_LOCATION, MOCK_FORECAST);
		assertThat(weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isPresent();
		assertThat(weatherCache.getForecast(MOCK_LOCATION, newTimeStamp)).isEmpty();

		weatherCache.updateCache(MOCK_LOCATION, newForeCast);
		final Optional<AbstractWeather> afterSecondUpdate = weatherCache.getForecast(MOCK_LOCATION, newTimeStamp);

		assertThat(afterSecondUpdate).isPresent();
		assertThat(afterSecondUpdate.get().getClouds()).isEqualTo(MOCK_CLOUD);
		assertThat(afterSecondUpdate.get().getMain()).isEqualTo(MOCK_MAIN);
	}

	@Test
	@DisplayName("Test update cache with current com.greencloud.application.weather for location not present")
	void testUpdateCacheCurrentWeatherLocationNotPresent() {
		assertThat(weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isEmpty();
		weatherCache.updateCache(MOCK_LOCATION, MOCK_CURRENT_WEATHER);

		final Optional<AbstractWeather> afterUpdate = weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME);

		assertThat(afterUpdate).isPresent();
		assertThat(afterUpdate.get().getClouds()).isEqualTo(MOCK_CLOUD);
		assertThat(afterUpdate.get().getMain()).isEqualTo(MOCK_MAIN);
	}

	@Test
	@DisplayName("Test update cache with current com.greencloud.application.weather for location present")
	void testUpdateCacheCurrentWeatherLocationPresent() {
		final Instant newTimeStamp = Instant.parse("2022-10-01T11:00:00.000Z");
		final CurrentWeather newCurrentWeather = ImmutableCurrentWeather.copyOf(MOCK_CURRENT_WEATHER)
				.withTimestamp(newTimeStamp);

		weatherCache.updateCache(MOCK_LOCATION, MOCK_CURRENT_WEATHER);
		assertThat(weatherCache.getForecast(MOCK_LOCATION, MOCK_TIME)).isPresent();
		assertThat(weatherCache.getForecast(MOCK_LOCATION, newTimeStamp)).isEmpty();

		weatherCache.updateCache(MOCK_LOCATION, newCurrentWeather);
		final Optional<AbstractWeather> afterSecondUpdate = weatherCache.getForecast(MOCK_LOCATION, newTimeStamp);

		assertThat(afterSecondUpdate).isPresent();
		assertThat(afterSecondUpdate.get().getClouds()).isEqualTo(MOCK_CLOUD);
		assertThat(afterSecondUpdate.get().getMain()).isEqualTo(MOCK_MAIN);

	}
}
