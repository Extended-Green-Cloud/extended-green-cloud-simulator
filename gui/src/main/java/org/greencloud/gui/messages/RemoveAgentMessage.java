package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRemoveAgentMessage.class)
@JsonDeserialize(as = ImmutableRemoveAgentMessage.class)
@Value.Immutable
public interface RemoveAgentMessage extends Message {

	String getAgentName();

	default String getType() {
		return "REMOVE_AGENT";
	}
}
