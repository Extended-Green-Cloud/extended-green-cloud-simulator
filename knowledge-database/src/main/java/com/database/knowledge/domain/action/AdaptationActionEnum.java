package com.database.knowledge.domain.action;

import java.util.Arrays;

import com.database.knowledge.exception.InvalidAdaptationActionException;

public enum AdaptationActionEnum {

	ADD_SERVER("Add server"),
	ADD_GREEN_SOURCE("Add green source"),
	INCREASE_DEADLINE_PRIORITY("Increase job deadline priority"),
	INCREASE_POWER_PRIORITY("Increase job power priority"),
	INCREASE_GREEN_SOURCE_PERCENTAGE("Increase Green Source selection chance"),
	INCREASE_GREEN_SOURCE_ERROR("Increase Green Source weather prediction error"),
	DECREASE_GREEN_SOURCE_ERROR("Decrement Green Source weather prediction error"),
	CONNECT_GREEN_SOURCE("Connecting Green Source");

	private final String name;

	AdaptationActionEnum(String name) {
		this.name = name;
	}

	public static AdaptationActionEnum getAdaptationActionEnumByName(final String actionName) {
		return Arrays.stream(values())
				.filter(action -> action.getName().equals(actionName))
				.findFirst()
				.orElseThrow(() -> new InvalidAdaptationActionException(actionName));
	}

	public String getName() {
		return name;
	}
}
