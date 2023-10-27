package com.gui.message;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableUpdateResourcesMessage.class)
@JsonDeserialize(as = ImmutableUpdateResourcesMessage.class)
@Value.Immutable
public interface UpdateResourcesMessage extends Message {

	Map<String, Resource> getResources();

	Double getPowerConsumption();

	Double getPowerConsumptionBackUp();

	String getAgentName();

	default String getType() {
		return "UPDATE_SERVER_RESOURCES";
	}
}
