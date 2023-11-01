package org.greencloud.gui.messages.domain;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableServerMaintenanceData.class)
@JsonDeserialize(as = ImmutableServerMaintenanceData.class)
public interface ServerMaintenanceData extends EventData {

	Map<String, Resource> getNewResources();
}
