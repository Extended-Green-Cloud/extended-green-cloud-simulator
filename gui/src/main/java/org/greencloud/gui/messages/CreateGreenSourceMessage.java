package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCreateGreenSourceMessage.class)
@JsonDeserialize(as = ImmutableCreateGreenSourceMessage.class)
@Value.Immutable
public interface CreateGreenSourceMessage extends Message {

	GreenSourceCreator getData();
}
