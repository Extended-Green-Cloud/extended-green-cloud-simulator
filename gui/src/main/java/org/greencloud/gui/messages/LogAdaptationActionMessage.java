package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.AdaptationLog;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableLogAdaptationActionMessage.class)
@JsonDeserialize(as = ImmutableLogAdaptationActionMessage.class)
@Value.Immutable
public interface LogAdaptationActionMessage extends Message {

	AdaptationLog getData();

	default String getType() {
		return "ADD_ADAPTATION_LOG";
	}
}
