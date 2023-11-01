package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateServerMessage.class)
@JsonDeserialize(as = ImmutableCreateServerMessage.class)
@Value.Immutable
public interface CreateServerMessage extends Message {

	ServerCreator getData();
}
