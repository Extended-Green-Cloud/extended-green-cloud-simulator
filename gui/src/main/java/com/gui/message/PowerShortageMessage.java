package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.PowerShortageData;

@Value.Immutable
@JsonSerialize(as = ImmutablePowerShortageMessage.class)
@JsonDeserialize(as = ImmutablePowerShortageMessage.class)
public interface PowerShortageMessage {

	String getAgentName();

	String getType();

	PowerShortageData getData();
}
