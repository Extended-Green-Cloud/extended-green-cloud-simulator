package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.EventData;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
@JsonDeserialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
public interface SwitchServerOnOffGUIMessage extends Message {

	String getAgentName();

	EventData getData();
}
