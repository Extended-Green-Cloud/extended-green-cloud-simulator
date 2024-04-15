package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRemoveAgentMessage.class)
@JsonDeserialize(as = ImmutableRemoveAgentMessage.class)
@Value.Immutable
public interface RemoveAgentMessage extends ExternalMessage {

	String getAgentName();

	default String getType() {
		return "REMOVE_AGENT";
	}
}
