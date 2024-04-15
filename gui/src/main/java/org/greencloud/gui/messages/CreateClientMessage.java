package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.JobCreator;
import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateClientMessage.class)
@JsonDeserialize(as = ImmutableCreateClientMessage.class)
@Value.Immutable
public interface CreateClientMessage extends ExternalMessage {

	String getClientName();
	JobCreator getData();
}
