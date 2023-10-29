package com.gui.message;

import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableUpdateResourcesMessage.class)
@JsonDeserialize(as = ImmutableUpdateResourcesMessage.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface UpdateResourcesMessage extends Message {

	Map<String, Resource> getResources();

	@Nullable
	Double getPowerConsumption();

	@Nullable
	Double getPowerConsumptionBackUp();

	String getAgentName();

	default String getType() {
		return "UPDATE_SERVER_RESOURCES";
	}
}
