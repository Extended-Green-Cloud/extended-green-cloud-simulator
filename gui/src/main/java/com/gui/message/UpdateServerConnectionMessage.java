package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;
import com.gui.message.domain.ServerConnection;

@JsonSerialize(as = ImmutableUpdateServerConnectionMessage.class)
@JsonDeserialize(as = ImmutableUpdateServerConnectionMessage.class)
@Value.Immutable
public interface UpdateServerConnectionMessage extends Message {

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
