package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableUpdateResourcesMessage.class)
@JsonDeserialize(as = ImmutableUpdateResourcesMessage.class)
@Value.Immutable
public interface UpdateResourcesMessage extends Message {

	HardwareResources getResources();

	Double getPowerConsumption();

	Double getPowerConsumptionBackUp();

	String getAgentName();

	default String getType() {
		return "UPDATE_SERVER_RESOURCES";
	}
}
