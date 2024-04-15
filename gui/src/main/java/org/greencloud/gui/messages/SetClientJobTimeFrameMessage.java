package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.JobTimeFrame;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@Value.Immutable
public interface SetClientJobTimeFrameMessage extends ExternalMessage {

	JobTimeFrame getData();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_TIME_FRAME";
	}
}
