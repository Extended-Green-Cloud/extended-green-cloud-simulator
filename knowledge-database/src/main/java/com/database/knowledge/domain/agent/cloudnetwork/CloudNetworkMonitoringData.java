package com.database.knowledge.domain.agent.cloudnetwork;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCloudNetworkMonitoringData.class)
@JsonDeserialize(as = ImmutableCloudNetworkMonitoringData.class)
@Value.Immutable
public interface CloudNetworkMonitoringData extends NetworkComponentMonitoringData {
}
