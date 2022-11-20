package com.greencloud.application.utils.domain;

import static com.greencloud.application.utils.TimeUtils.useMockTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimerUnitTest {

	private Timer timer;

	@BeforeEach
	void setUp() {
		timer = new Timer();
	}

	@Test
	@DisplayName("Test set timer start")
	void testStartTimeMeasure() {
		final Instant start = Instant.parse("2022-01-01T10:00:00.000Z");
		useMockTime(start, ZoneId.of("UTC"));
		timer.startTimeMeasure();
		assertThat(timer.getTimeStart().get()).isEqualTo(start);
	}

	@Test
	@DisplayName("Test get elapsed time")
	void testStopTimeMeasure() {
		useMockTime(Instant.parse("2022-01-01T10:00:00.000Z"), ZoneId.of("UTC"));
		timer.startTimeMeasure();
		useMockTime(Instant.parse("2022-01-01T10:00:10.000Z"), ZoneId.of("UTC"));

		assertThat(timer.stopTimeMeasure()).isEqualTo(10000);
	}
}
