package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetClientJobStatusMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobStatusMessage.class)
@Value.Immutable
public interface SetClientJobStatusMessage extends Message {

	String getStatus();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_STATUS";
	}
}
