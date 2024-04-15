package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.greencloud.gui.messages.domain.ServerMaintenanceData;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerMaintenanceMessage.class)
@JsonDeserialize(as = ImmutableServerMaintenanceMessage.class)
public interface ServerMaintenanceMessage extends ExternalMessage {

	String getAgentName();

	ServerMaintenanceData getData();
}
