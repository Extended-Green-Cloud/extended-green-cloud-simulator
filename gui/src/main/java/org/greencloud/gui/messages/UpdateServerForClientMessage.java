package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateServerForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateServerForClientMessage.class)
@Value.Immutable
public interface UpdateServerForClientMessage extends Message {

	/**
	 * @return name of the client agent
	 */
	String getAgentName();

	/**
	 * @return name of the server executing client job
	 */
	String getServerName();

	default String getType() {
		return "UPDATE_SERVER_FOR_CLIENT";
	}
}
