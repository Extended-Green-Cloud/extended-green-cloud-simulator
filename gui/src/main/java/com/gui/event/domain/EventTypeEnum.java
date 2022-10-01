package com.gui.event.domain;

/**
 * Enum defining types of the environment events
 */
public enum EventTypeEnum {
	POWER_SHORTAGE("MAKE POWER SHORTAGE", "FINISH POWER SHORTAGE");

	private final String eventLabelStart;
	private final String eventLabelFinish;

	/**
	 * Default enum constructor
	 *
	 * @param eventLabelStart  label indicating that the event can be started
	 * @param eventLabelFinish label indicating that the event can be finished (possibly null for one shot events)
	 */
	EventTypeEnum(String eventLabelStart, String eventLabelFinish) {
		this.eventLabelStart = eventLabelStart;
		this.eventLabelFinish = eventLabelFinish;
	}

	/**
	 * @return label indicating that the event can be started
	 */
	public String getEventLabelStart() {
		return null;
	}

	/**
	 * @return label indicating that the event can be finished
	 */
	public String getEventLabelFinish() {
		return null;
	}
}
