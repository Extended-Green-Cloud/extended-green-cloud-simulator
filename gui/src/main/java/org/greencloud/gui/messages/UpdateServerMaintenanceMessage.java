package org.greencloud.gui.messages;

import javax.annotation.Nullable;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateServerMaintenanceMessage.class)
@JsonDeserialize(as = ImmutableUpdateServerMaintenanceMessage.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface UpdateServerMaintenanceMessage extends Message {


	String getAgentName();
	String getState();
	Boolean getResult();

	@Nullable
	Boolean getError();

	default String getType() {
		return "UPDATE_SERVER_MAINTENANCE_STATE";
	}
}
