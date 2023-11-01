package org.greencloud.gui.messages.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerConnection.class)
@JsonDeserialize(as = ImmutableServerConnection.class)
public interface ServerConnection {

	/**
	 * @return flag indicating if the server should be connected or disconnected
	 */
	boolean isConnected();

	/**
	 * @return name of the server to connect/disconnect to given green source
	 */
	String getServerName();
}
