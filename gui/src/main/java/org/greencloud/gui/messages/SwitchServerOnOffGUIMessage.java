package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.EventData;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
@JsonDeserialize(as = ImmutableSwitchServerOnOffGUIMessage.class)
public interface SwitchServerOnOffGUIMessage extends ExternalMessage {

	String getAgentName();

	EventData getData();
}
