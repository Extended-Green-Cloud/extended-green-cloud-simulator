package org.greencloud.gui.messages;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateDefaultResourcesMessage.class)
@JsonDeserialize(as = ImmutableUpdateDefaultResourcesMessage.class)
@Value.Immutable
public interface UpdateDefaultResourcesMessage extends ExternalMessage {

	Map<String, Resource> getResources();

	String getAgentName();

	default String getType() {
		return "UPDATE_DEFAULT_RESOURCES";
	}
}
