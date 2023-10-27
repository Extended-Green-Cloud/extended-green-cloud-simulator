package com.gui.event.domain;

import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;

import java.io.Serializable;

/**
 * Enum defining types of the environment eventsQueue
 */
public enum EventTypeEnum implements Serializable {

	POWER_SHORTAGE_EVENT(POWER_SHORTAGE_ERROR_RULE),
	DISABLE_SERVER_EVENT(ADAPTATION_REQUEST_RULE),
	ENABLE_SERVER_EVENT(ADAPTATION_REQUEST_RULE),
	WEATHER_DROP_EVENT(WEATHER_DROP_ERROR_RULE);

	final String ruleType;

	EventTypeEnum(final String ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleType() {
		return ruleType;
	}
}
