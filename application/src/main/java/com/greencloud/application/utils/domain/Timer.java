package com.greencloud.application.utils.domain;

import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

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
	public void startTimeMeasure() {
		timeStart.set(getCurrentTime());
	}

	/**
	 * Method ends the time measure
	 *
	 * @return elapsed time in ms
	 */
	public long stopTimeMeasure() {
		return Duration.between(timeStart.get(), getCurrentTime()).toMillis();
	}

	public AtomicReference<Instant> getTimeStart() {
		return timeStart;
	}
}
