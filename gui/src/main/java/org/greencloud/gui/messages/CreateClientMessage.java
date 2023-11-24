package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateClientMessage.class)
@JsonDeserialize(as = ImmutableCreateClientMessage.class)
@Value.Immutable
public interface CreateClientMessage extends Message {

	String getClientName();
	JobCreator getData();
}
