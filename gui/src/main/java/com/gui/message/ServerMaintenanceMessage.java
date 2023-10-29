package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;
import com.gui.message.domain.ServerMaintenanceData;

@Value.Immutable
@JsonSerialize(as = ImmutableServerMaintenanceMessage.class)
@JsonDeserialize(as = ImmutableServerMaintenanceMessage.class)
public interface ServerMaintenanceMessage extends Message {

	String getAgentName();

	ServerMaintenanceData getData();
}
