package org.greencloud.gui.messages;

import java.time.Instant;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateFinalExecutionDateForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateFinalExecutionDateForClientMessage.class)
@Value.Immutable
public interface UpdateFinalExecutionDateForClientMessage extends ExternalMessage {

	/**
	 * @return name of the client agent
	 */
	String getAgentName();

	/**
	 * @return job execution finish date
	 */
	Instant getFinalExecutionDate();

	default String getType() {
		return "UPDATE_FINAL_EXECUTION_DATE_FOR_CLIENT";
	}
}
