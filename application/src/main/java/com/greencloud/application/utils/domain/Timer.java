package com.greencloud.application.utils.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class contains methods which allow to measure time between two events
 */
public class Timer {

	private final AtomicReference<Instant> timeStart;

	public Timer() {
		this.timeStart = new AtomicReference<>();
	}

	/**
	 * Method starts the timer
	 */
	public void startTimeMeasure(final Instant startTime) {
		timeStart.set(startTime);
	}

	/**
	 * Method ends the time measure
	 *
	 * @return elapsed time in ms
	 */
	public long stopTimeMeasure(final Instant stopTime) {
		return Duration.between(timeStart.get(), stopTime).toMillis();
	}

	public AtomicReference<Instant> getTimeStart() {
		return timeStart;
	}
}
