package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetClientJobStatusMessage.class)
@JsonDeserialize(as = ImmutableSetClientJobStatusMessage.class)
@Value.Immutable
public interface SetClientJobStatusMessage {

	String getData();

	String getAgentName();

	default String getType() {
		return "SET_CLIENT_JOB_STATUS";
	}
}
