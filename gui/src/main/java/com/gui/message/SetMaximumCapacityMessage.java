package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Capacity;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableSetMaximumCapacityMessage.class)
@JsonDeserialize(as = ImmutableSetMaximumCapacityMessage.class)
@Value.Immutable
public interface SetMaximumCapacityMessage extends Message {

	String getAgentName();

	Capacity getData();

	default String getType() {
		return "SET_MAXIMUM_CAPACITY";
	}
}
