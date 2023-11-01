package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.JobTimeFrame;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@Value.Immutable
public interface SetClientJobTimeFrameMessage extends Message {

	JobTimeFrame getData();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_TIME_FRAME";
	}
}
