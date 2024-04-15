package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.EventData;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutablePowerShortageMessage.class)
@JsonDeserialize(as = ImmutablePowerShortageMessage.class)
public interface PowerShortageMessage extends ExternalMessage {

	String getAgentName();

	EventData getData();
}
