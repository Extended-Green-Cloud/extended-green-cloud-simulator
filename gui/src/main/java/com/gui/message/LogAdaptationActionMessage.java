package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.AdaptationLog;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableLogAdaptationActionMessage.class)
@JsonDeserialize(as = ImmutableLogAdaptationActionMessage.class)
@Value.Immutable
public interface LogAdaptationActionMessage extends Message {

	AdaptationLog getData();

	default String getType() {
		return "ADD_ADAPTATION_LOG";
	}
}
