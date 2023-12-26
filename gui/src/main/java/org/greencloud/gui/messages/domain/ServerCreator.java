package org.greencloud.gui.messages.domain;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerCreator.class)
@JsonDeserialize(as = ImmutableServerCreator.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ServerCreator extends EventData {

	String getName();

	String getRegionalManager();

	Double getMaxPower();

	Double getIdlePower();

	Map<String, Resource> getResources();

	Long getJobProcessingLimit();

	Double getPrice();

}
