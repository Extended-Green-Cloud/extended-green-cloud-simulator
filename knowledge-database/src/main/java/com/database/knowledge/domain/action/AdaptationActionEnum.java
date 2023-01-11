package com.database.knowledge.domain.action;

import java.util.Arrays;

import com.database.knowledge.exception.InvalidAdaptationActionException;

public enum AdaptationActionEnum {

	ADD_SERVER("Add server"),
	ADD_GREEN_SOURCE("Add green source"),
	INCREASE_DEADLINE_PRIORITY("Increase job deadline priority"),
	CHANGE_GREEN_SOURCE_WEIGHT("Change Green Source selection weight"),
	INCREASE_POWER_PRIORITY("Increase job power priority"),
	INCREASE_GREEN_SOURCE_ERROR("Increase Green Source weather prediction error"),
	DECREASE_GREEN_SOURCE_ERROR("Decrement Green Source weather prediction error"),
	CONNECT_GREEN_SOURCE("Connecting Green Source"),
	DISCONNECT_GREEN_SOURCE("Disconnecting Green Source"),
	DISABLE_SERVER("Disable server");

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
