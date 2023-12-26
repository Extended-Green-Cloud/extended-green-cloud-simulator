package org.greencloud.gui.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.enums.event.EventTypeEnum;
import org.greencloud.gui.agents.egcs.EGCSNode;

/**
 * Class represents the abstract event which may occur in the environment
 */
public abstract class AbstractEvent implements Serializable {

	protected String agentName;
	protected EventTypeEnum eventTypeEnum;
	protected Instant occurrenceTime;

	/**
	 * Default event constructor
	 *
	 * @param eventTypeEnum  type of the event
	 * @param occurrenceTime time when the event occurs
	 */
	protected AbstractEvent(final EventTypeEnum eventTypeEnum, final Instant occurrenceTime, final String agentName) {
		this.eventTypeEnum = eventTypeEnum;
		this.occurrenceTime = occurrenceTime;
		this.agentName = agentName;
	}

	/**
	 * Method responsible for triggering a given event
	 *
	 * @param agentNodes all nodes present in the system
	 */
	public abstract void trigger(final Map<String, EGCSNode> agentNodes);

	public Instant getOccurrenceTime() {
		return occurrenceTime;
	}

	public EventTypeEnum getEventTypeEnum() {
		return eventTypeEnum;
	}
}
