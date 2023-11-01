package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.AdaptationAction;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateAdaptationActionMessage.class)
@JsonDeserialize(as = ImmutableUpdateAdaptationActionMessage.class)
public interface UpdateAdaptationActionMessage extends Message {

	AdaptationAction getData();

	default String getType() {
		return "UPDATE_ADAPTATION_ACTION";
	}
}
