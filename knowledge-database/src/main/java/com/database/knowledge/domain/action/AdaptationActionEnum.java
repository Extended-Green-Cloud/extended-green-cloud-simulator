package com.database.knowledge.domain.action;

import java.util.Arrays;

import com.database.knowledge.exception.InvalidAdaptationActionException;

public enum AdaptationActionEnum {

	ADD_SERVER("Add server"),
	INCREASE_DEADLINE_PRIO("Increase job deadline priority"),
	INCREASE_POWER_PRIO("Increase job power priority"),
	INCREASE_GREEN_SOURCE_PERCENTAGE("Increase Green Source selection chance"),
	INCREASE_GREEN_SOURCE_ERROR("Increase Green Source weather prediction error"),
	ADD_GREEN_SOURCE("Add Green Source");

	private final String name;

	AdaptationActionEnum(String name) {
		this.name = name;
	}

	public static AdaptationActionEnum getAdaptationActionByName(final String actionName) {
		return Arrays.stream(values()).
				filter(action -> action.getName().equals(actionName))
				.findFirst()
				.orElseThrow(() -> new InvalidAdaptationActionException(actionName));
	}

	public String getName() {
		return name;
	}
}
