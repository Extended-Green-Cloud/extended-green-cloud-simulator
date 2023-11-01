package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.EventData;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutablePowerShortageMessage.class)
@JsonDeserialize(as = ImmutablePowerShortageMessage.class)
public interface PowerShortageMessage extends Message {

	String getAgentName();

	EventData getData();
}
