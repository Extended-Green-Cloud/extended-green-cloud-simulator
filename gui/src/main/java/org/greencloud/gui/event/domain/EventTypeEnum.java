package org.greencloud.gui.event.domain;

import static org.greencloud.commons.enums.rules.RuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.RuleType.AGENT_CREATION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.AGENT_MODIFY_RULE_SET_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_MAINTENANCE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;

import java.io.Serializable;

/**
 * Enum defining types of the environment eventsQueue
 */
public enum EventTypeEnum implements Serializable {

	POWER_SHORTAGE_EVENT(POWER_SHORTAGE_ERROR_RULE),
	DISABLE_SERVER_EVENT(ADAPTATION_REQUEST_RULE),
	ENABLE_SERVER_EVENT(ADAPTATION_REQUEST_RULE),
	SERVER_MAINTENANCE_EVENT(SERVER_MAINTENANCE_RULE),
	CLIENT_CREATION_EVENT(AGENT_CREATION_RULE),
	GREEN_SOURCE_CREATION_EVENT(AGENT_CREATION_RULE),
	SERVER_CREATION_EVENT(AGENT_CREATION_RULE),
	MODIFY_RULE_SET(AGENT_MODIFY_RULE_SET_RULE),
	WEATHER_DROP_EVENT(WEATHER_DROP_ERROR_RULE);

	final String ruleType;

	EventTypeEnum(final String ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleType() {
		return ruleType;
	}
}
