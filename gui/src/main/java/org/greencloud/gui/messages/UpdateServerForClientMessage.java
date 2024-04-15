package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateServerForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateServerForClientMessage.class)
@Value.Immutable
public interface UpdateServerForClientMessage extends ExternalMessage {

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
