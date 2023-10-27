package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.EventData;
import com.gui.message.domain.Message;

@Value.Immutable
@JsonSerialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
@JsonDeserialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
public interface SwitchServerOnOffGUIMessage extends Message {

	String getAgentName();

	EventData getEventData();
}
