package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableIsActiveMessage.class)
@JsonDeserialize(as = ImmutableIsActiveMessage.class)
@Value.Immutable
public interface IsActiveMessage extends Message {

	boolean getData();

	String getAgentName();

	default String getType() {
		return "SET_IS_ACTIVE";
	}
}
