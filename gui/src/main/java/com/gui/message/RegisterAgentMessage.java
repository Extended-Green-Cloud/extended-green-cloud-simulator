package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.AgentArgs;

@JsonSerialize(as = ImmutableRegisterAgentMessage.class)
@JsonDeserialize(as = ImmutableRegisterAgentMessage.class)
@Value.Immutable
public interface RegisterAgentMessage {

	String getAgentType();

	AgentArgs getData();

	default String getType() {
		return "REGISTER_AGENT";
	}
}
