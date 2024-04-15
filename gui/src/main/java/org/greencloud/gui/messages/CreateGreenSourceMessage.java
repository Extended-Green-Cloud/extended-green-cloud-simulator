package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateGreenSourceMessage.class)
@JsonDeserialize(as = ImmutableCreateGreenSourceMessage.class)
@Value.Immutable
public interface CreateGreenSourceMessage extends ExternalMessage {

	GreenSourceCreator getData();
}
