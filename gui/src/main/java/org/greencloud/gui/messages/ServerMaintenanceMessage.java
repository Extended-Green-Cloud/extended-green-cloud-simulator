package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.greencloud.gui.messages.domain.ServerMaintenanceData;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerMaintenanceMessage.class)
@JsonDeserialize(as = ImmutableServerMaintenanceMessage.class)
public interface ServerMaintenanceMessage extends Message {

	String getAgentName();

	ServerMaintenanceData getData();
}
