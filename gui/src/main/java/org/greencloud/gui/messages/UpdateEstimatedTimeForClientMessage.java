package org.greencloud.gui.messages;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateEstimatedTimeForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateEstimatedTimeForClientMessage.class)
@Value.Immutable
public interface UpdateEstimatedTimeForClientMessage extends ExternalMessage {

	/**
	 * @return name of the client agent
	 */
	String getAgentName();

	/**
	 * @return estimated job execution time
	 */
	Double getEstimatedTime();

	default String getType() {
		return "UPDATE_ESTIMATED_TIME_FOR_CLIENT";
	}
}
