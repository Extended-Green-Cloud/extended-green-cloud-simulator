package com.gui.event;

import java.io.Serializable;
import java.time.Instant;

import com.gui.event.domain.EventTypeEnum;

/**
 * Class represents the abstract event which may occur in the environment
 */
public abstract class AbstractEvent implements Serializable {

	protected EventTypeEnum eventTypeEnum;
	protected Instant occurrenceTime;

	/**
	 * Default event constructor
	 *
	 * @param eventTypeEnum  type of the event
	 * @param occurrenceTime time when the event occurs
	 */
	protected AbstractEvent(final EventTypeEnum eventTypeEnum, final Instant occurrenceTime) {
		this.eventTypeEnum = eventTypeEnum;
		this.occurrenceTime = occurrenceTime;
	}

	/**
	 * @return time when the event will occur
	 */
	public Instant getOccurrenceTime() {
		return occurrenceTime;
	}

	public EventTypeEnum getEventTypeEnum() {
		return eventTypeEnum;
	}
}
