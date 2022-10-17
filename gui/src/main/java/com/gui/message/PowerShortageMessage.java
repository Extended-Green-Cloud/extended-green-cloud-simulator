package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.event.domain.EventTypeEnum;
import com.gui.message.domain.PowerShortageData;

@Value.Immutable
@JsonSerialize(as = PowerShortageMessage.class)
@JsonDeserialize(as = PowerShortageMessage.class)
public interface PowerShortageMessage {

	String getAgentName();

	EventTypeEnum getType();

	PowerShortageData getData();
}
