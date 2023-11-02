package org.greencloud.commons.domain.timer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.Setter;

/**
 * Class representing simple timer which allow to measure time between two events
 */
@Getter
@Setter
public class Timer {

	private final AtomicReference<Instant> timeStart;
	private final AtomicBoolean isStarted;

	public Timer() {
		this.timeStart = new AtomicReference<>();
		this.isStarted = new AtomicBoolean(false);
	}

	/**
	 * Method starts the timer
	 */
	public void startTimeMeasure(final Instant startTime) {
		timeStart.set(startTime);
		isStarted.set(true);
	}

	/**
	 * Method ends the time measure
	 *
	 * @return elapsed time in ms
	 */
	public long stopTimeMeasure(final Instant stopTime) {
		isStarted.set(false);
		return Duration.between(timeStart.get(), stopTime).toMillis();
	}
}
