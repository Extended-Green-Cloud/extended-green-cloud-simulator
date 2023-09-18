package com.gui.event.domain;

import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;

import java.io.Serializable;

import org.greencloud.commons.enums.rules.RuleType;

/**
 * Enum defining types of the environment eventsQueue
 */
public enum EventTypeEnum implements Serializable {

	POWER_SHORTAGE_EVENT(POWER_SHORTAGE_ERROR_RULE),
	WEATHER_DROP_EVENT(WEATHER_DROP_ERROR_RULE);

	final RuleType ruleType;

	EventTypeEnum(final RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public RuleType getRuleType() {
		return ruleType;
	}
}
