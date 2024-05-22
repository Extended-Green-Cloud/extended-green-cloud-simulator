package org.greencloud.commons.enums.adaptation;

import static java.util.Arrays.stream;

import org.greencloud.commons.exception.InvalidAdaptationActionException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AdaptationActionTypeEnum {

	ADD_SERVER("Add server"),
	ADD_GREEN_SOURCE("Add green source"),
	CHANGE_GREEN_SOURCE_WEIGHT("Change Green Source selection weight"),
	INCREASE_GREEN_SOURCE_ERROR("Increase Green Source weather prediction error"),
	DECREASE_GREEN_SOURCE_ERROR("Decrement Green Source weather prediction error"),
	CONNECT_GREEN_SOURCE("Connecting Green Source"),
	DISCONNECT_GREEN_SOURCE("Disconnecting Green Source"),
	DISABLE_SERVER("Disable server"),
	ENABLE_SERVER("Enable server");

	private final String name;

	public static AdaptationActionTypeEnum getAdaptationActionEnumByName(final String actionName) {
		return stream(values())
				.filter(action -> action.getName().equals(actionName))
				.findFirst()
				.orElseThrow(() -> new InvalidAdaptationActionException(actionName));
	}
}
