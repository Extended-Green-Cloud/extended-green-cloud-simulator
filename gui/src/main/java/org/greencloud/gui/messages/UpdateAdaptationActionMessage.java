package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.AdaptationAction;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableUpdateAdaptationActionMessage.class)
@JsonDeserialize(as = ImmutableUpdateAdaptationActionMessage.class)
public interface UpdateAdaptationActionMessage extends ExternalMessage {

	AdaptationAction getData();

	default String getType() {
		return "UPDATE_ADAPTATION_ACTION";
	}
}
