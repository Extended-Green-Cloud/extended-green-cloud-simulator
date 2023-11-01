package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
