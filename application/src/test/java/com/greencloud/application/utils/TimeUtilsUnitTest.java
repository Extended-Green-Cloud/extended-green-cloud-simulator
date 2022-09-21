package com.greencloud.application.utils;

import static com.greencloud.application.utils.TimeUtils.convertToInstantTime;
import static com.greencloud.application.utils.TimeUtils.convertToSimulationTime;
import static com.greencloud.application.utils.TimeUtils.differenceInHours;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTimeMinusError;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.greencloud.application.exception.IncorrectTaskDateException;

class TimeUtilsUnitTest {

	private static Stream<Arguments> parametersTimeStampTest() {
		return Stream.of(
				Arguments.of(Instant.parse("2022-01-01T11:30:00.000Z"), true),
				Arguments.of(Instant.parse("2022-01-01T13:30:00.000Z"), false),
				Arguments.of(Instant.parse("2022-01-01T10:00:00.000Z"), true),
				Arguments.of(Instant.parse("2022-01-01T12:00:00.000Z"), false)
		);
	}

	@Test
	@DisplayName("Test converting to Instant for correct String")
	void testConvertToInstantForCorrectDate() {
		final String mockStringDate = "01/01/2022 10:00";
		final Instant result = convertToInstantTime(mockStringDate);
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		calendar.setTime(Date.from(result));

		assertThat(calendar.get(Calendar.MONTH)).isZero();
		assertThat(calendar.get(Calendar.HOUR)).isEqualTo(10);
		assertThat(calendar.get(Calendar.MINUTE)).isZero();
	}

	@Test
	@DisplayName("Test converting to Instant for incorrect String")
	void testConvertToInstantForIncorrectDate() {
		final String mockStringDate = "01.01.2022 10:00";
		assertThatThrownBy(() -> convertToInstantTime(mockStringDate)).isInstanceOf(IncorrectTaskDateException.class)
				.hasMessage("The provided execution date has incorrect format");
	}

	@Test
	@DisplayName("Test get current time at UTC Zone")
	void testGetCurrentTimeUTCZone() {
		final Instant mockInstant = Instant.parse("2022-01-01T10:00:00.000Z");
		useMockTime(Instant.parse("2022-01-01T10:00:00.000Z"), ZoneId.of("UTC"));

		final Instant instant = getCurrentTime();
		assertThat(instant).isEqualTo(mockInstant);
	}

	@Test
	@DisplayName("Test get current time at other zone")
	void testGetCurrentTimeOtherZone() {
		final Instant mockInstant = Instant.parse("2022-01-01T09:00:00.000Z");
		final ZoneId zoneId = ZoneId.of("Europe/Berlin");
		useMockTime(LocalDateTime.parse("2022-01-01T10:00:00.000").atZone(zoneId).toInstant(), zoneId);

		final Instant instant = getCurrentTime();
		assertThat(instant).isEqualTo(mockInstant);
	}

	@Test
	@DisplayName("Test get current time minus error")
	void testGetCurrentTimeMinusError() {
		final Instant mockInstant = Instant.parse("2022-01-01T09:55:00.000Z");
		useMockTime(Instant.parse("2022-01-01T10:00:00.000Z"), ZoneId.of("UTC"));

		final Instant instant = getCurrentTimeMinusError();
		assertThat(instant).isEqualTo(mockInstant);
	}

	@Test
	@DisplayName("Test converting 2h to simulation time")
	void testConversionFullHoursToSimulationTime() {
		final long seconds = 7200;
		final long expectedMilliseconds = 6000;

		final long resultMilliseconds = convertToSimulationTime(seconds);
		assertThat(resultMilliseconds).isEqualTo(expectedMilliseconds);
	}

	@Test
	@DisplayName("Test converting 15 minutes to simulation time")
	void testConversionMinutesToSimulationTime() {
		final long seconds = 900;
		final long expectedMilliseconds = 750;

		final long resultMilliseconds = convertToSimulationTime(seconds);
		assertThat(resultMilliseconds).isEqualTo(expectedMilliseconds);
	}

	@Test
	@DisplayName("Test computing time difference in hours")
	void testComputingTimeDifferenceInHours() {
		final Instant mockInstant1 = Instant.parse("2022-01-01T09:45:00.000Z");
		final Instant mockInstant2 = Instant.parse("2022-01-01T10:55:00.000Z");

		final double hourDifference = differenceInHours(mockInstant1, mockInstant2);
		assertThat(hourDifference).isCloseTo(1.16, Percentage.withPercentage(1));
	}

	@ParameterizedTest
	@MethodSource("parametersTimeStampTest")
	void testIsWithinTimeStamp(final Instant timeToCheck, final boolean expectedResult) {
		final Instant startTime = Instant.parse("2022-01-01T10:00:00.000Z");
		final Instant endTime = Instant.parse("2022-01-01T12:00:00.000Z");

		assertThat(isWithinTimeStamp(startTime, endTime, timeToCheck)).isEqualTo(expectedResult);
	}
}
