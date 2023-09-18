package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.agent.AgentArgs;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableRegisterAgentMessage.class)
@JsonDeserialize(as = ImmutableRegisterAgentMessage.class)
@Value.Immutable
public interface RegisterAgentMessage extends Message {

	String getAgentType();

	AgentArgs getData();

	default String getType() {
		return "REGISTER_AGENT";
	}
}
