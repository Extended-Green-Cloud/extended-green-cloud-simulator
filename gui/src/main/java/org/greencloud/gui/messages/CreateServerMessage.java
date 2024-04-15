package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateServerMessage.class)
@JsonDeserialize(as = ImmutableCreateServerMessage.class)
@Value.Immutable
public interface CreateServerMessage extends ExternalMessage {

	ServerCreator getData();
}
