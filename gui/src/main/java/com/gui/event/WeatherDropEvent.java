package com.gui.event;

import static com.gui.event.domain.EventTypeEnum.WEATHER_DROP_EVENT;

import java.time.Instant;

import com.gui.message.WeatherDropMessage;

import lombok.Getter;

/**
 * Event simulating long-term power shortage which affects given selected region of the system
 */
@Getter
public class WeatherDropEvent extends AbstractEvent {

	final long duration;

	/**
	 * Default event constructor
	 *
	 * @param occurrenceTime time when the event occurs
	 * @param duration       duration of weather drop
	 */
	protected WeatherDropEvent(final Instant occurrenceTime, final long duration) {
		super(WEATHER_DROP_EVENT, occurrenceTime);
		this.duration = duration;
	}

	public WeatherDropEvent(WeatherDropMessage weatherDropMessage) {
		this(weatherDropMessage.getData().getOccurrenceTime(), weatherDropMessage.getData().getDuration());
	}

}
