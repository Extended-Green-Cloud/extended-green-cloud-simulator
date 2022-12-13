package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.JobTimeFrame;

@JsonSerialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobTimeFrameMessage.class)
@Value.Immutable
public interface SetClientJobTimeFrameMessage {

	JobTimeFrame getData();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_TIME_FRAME";
	}
}
