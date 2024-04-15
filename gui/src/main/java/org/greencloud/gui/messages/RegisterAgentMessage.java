package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jrba.agentmodel.domain.args.AgentArgs;

@JsonSerialize(as = ImmutableRegisterAgentMessage.class)
@JsonDeserialize(as = ImmutableRegisterAgentMessage.class)
@Value.Immutable
public interface RegisterAgentMessage extends ExternalMessage {

	String getAgentType();

	AgentArgs getData();

	default String getType() {
		return "REGISTER_AGENT";
	}
}
