package com.greencloud.application.weather.cache;

import static com.greencloud.application.constants.CacheTestConstants.MOCK_CLOUD;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_CNT;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_CURRENT_WEATHER;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_FORECAST;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_FUTURE_WEATHER;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_MAIN;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.weather.domain.AbstractWeather;
import com.greencloud.application.weather.domain.Forecast;
import com.greencloud.application.weather.domain.ImmutableForecast;

class ForecastTimetableUnitTest {

	private ForecastTimetable forecastTimetable;

	@BeforeEach
	void init() {
		final Forecast emptyForecast = ImmutableForecast.builder().cnt(MOCK_CNT).build();
		forecastTimetable = new ForecastTimetable(emptyForecast);
	}

	@Test
	@DisplayName("Test constructor for empty forecast")
	void testConstructorForEmptyForecast() {
		assertThat(forecastTimetable.getTimetable()).isEmpty();
	}

	@Test
	@DisplayName("Test constructor for non empty forecast")
	void testConstructorForNonEmptyForecast() {
		final ForecastTimetable result = new ForecastTimetable(MOCK_FORECAST);

		assertThat(result.getTimetable()).hasSize(1);
		assertThat(result.getTimetable().get(MOCK_TIME).getClouds().getAll()).isEqualTo(5.5);
		assertNotNull(result.getTimetable().get(MOCK_TIME));
	}

	@Test
	@DisplayName("Test constructor for current com.greencloud.application.weather")
	void testConstructorForCurrentWeather() {
		final ForecastTimetable result = new ForecastTimetable(MOCK_CURRENT_WEATHER);

		assertThat(result.getTimetable()).hasSize(1);
		assertThat(result.getTimetable().get(MOCK_TIME).getMain().getFeelsLike()).isEqualTo(15.0);
		assertNotNull(result.getTimetable().get(MOCK_TIME));
	}

	@Test
	@DisplayName("Test update timetable by new forecast")
	void testUpdateTimeTableByNewForecast() {
		assertThat(forecastTimetable.getTimetable()).isEmpty();
		forecastTimetable.updateTimetable(MOCK_FORECAST);

		assertThat(forecastTimetable.getTimetable()).hasSize(1);
		assertNotNull(forecastTimetable.getTimetable().get(MOCK_TIME));
	}

	@Test
	@DisplayName("Test update timetable by existing forecast")
	void testUpdateTimeTableByExistingForecast() {
		assertThat(forecastTimetable.getTimetable()).isEmpty();

		forecastTimetable.updateTimetable(MOCK_FORECAST);

		assertNotNull(forecastTimetable.getTimetable().get(MOCK_TIME));
		assertThat(forecastTimetable.getTimetable()).hasSize(1);

		forecastTimetable.updateTimetable(MOCK_FORECAST);

		assertThat(forecastTimetable.getTimetable()).hasSize(1);
	}

	@Test
	@DisplayName("Test update timetable by current com.greencloud.application.weather")
	void testUpdateTimeTableByCurrentWeather() {
		assertThat(forecastTimetable.getTimetable()).isEmpty();
		forecastTimetable.updateTimetable(MOCK_CURRENT_WEATHER);

		assertThat(forecastTimetable.getTimetable()).hasSize(1);
		assertNotNull(forecastTimetable.getTimetable().get(MOCK_TIME));
	}

	@Test
	@DisplayName("Test update timetable by existing current com.greencloud.application.weather")
	void testUpdateTimeTableByExistingCurrentWeather() {
		assertThat(forecastTimetable.getTimetable()).isEmpty();
		forecastTimetable.updateTimetable(MOCK_CURRENT_WEATHER);

		assertNotNull(forecastTimetable.getTimetable().get(MOCK_TIME));
		assertThat(forecastTimetable.getTimetable()).hasSize(1);

		forecastTimetable.updateTimetable(MOCK_CURRENT_WEATHER);

		assertThat(forecastTimetable.getTimetable()).hasSize(1);
	}

	@Test
	@DisplayName("Test getting future com.greencloud.application.weather for empty timetable")
	void testGetFutureWeatherEmptyTimetable() {
		assertThatThrownBy(() -> forecastTimetable.getFutureWeather(MOCK_TIME))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("No value present");
	}

	@Test
	@DisplayName("Test getting future com.greencloud.application.weather for too big time difference")
	void testGetFutureWeatherLargeExpiration() {
		final Forecast mockForecast = ImmutableForecast.builder()
				.cnt(MOCK_CNT)
				.list(List.of(MOCK_FUTURE_WEATHER))
				.build();
		final Instant mockInstant = Instant.parse("2022-01-01T18:00:00.000Z");
		forecastTimetable.updateTimetable(mockForecast);
		final Optional<AbstractWeather> result = forecastTimetable.getFutureWeather(mockInstant);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Test getting future com.greencloud.application.weather for correct timestamp")
	void testGetFutureWeatherCorrectTimestamp() {
		final Forecast mockForecast = ImmutableForecast.builder()
				.cnt(MOCK_CNT)
				.list(List.of(MOCK_FUTURE_WEATHER))
				.build();
		final Instant mockInstant = Instant.parse("2022-01-01T13:00:00.000Z");
		forecastTimetable.updateTimetable(mockForecast);
		final Optional<AbstractWeather> result = forecastTimetable.getFutureWeather(mockInstant);

		assertThat(result).isPresent();
		assertThat(result.get().getMain()).isEqualTo(MOCK_MAIN);
		assertThat(result.get().getClouds()).isEqualTo(MOCK_CLOUD);
	}
}
