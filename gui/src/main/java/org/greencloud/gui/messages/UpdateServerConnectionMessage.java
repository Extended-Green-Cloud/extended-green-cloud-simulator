package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.greencloud.gui.messages.domain.ServerConnection;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateServerConnectionMessage.class)
@JsonDeserialize(as = ImmutableUpdateServerConnectionMessage.class)
@Value.Immutable
public interface UpdateServerConnectionMessage extends ExternalMessage {

	/**
	 * @return name of the green source agent
	 */
	String getAgentName();

	/**
	 * @return message connection details
	 */
	ServerConnection getData();

	default String getType() {
		return "UPDATE_SERVER_CONNECTION";
	}
}
