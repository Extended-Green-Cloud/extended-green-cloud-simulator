package com.greencloud.application.utils.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

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
		timer.startTimeMeasure(start);
		assertThat(timer.getTimeStart().get()).isEqualTo(start);
	}

	@Test
	@DisplayName("Test get elapsed time")
	void testStopTimeMeasure() {
		timer.startTimeMeasure(Instant.parse("2022-01-01T10:00:00.000Z"));
		assertThat(timer.stopTimeMeasure(Instant.parse("2022-01-01T10:00:10.000Z"))).isEqualTo(10000);
	}
}
