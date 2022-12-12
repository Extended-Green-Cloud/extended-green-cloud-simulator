package com.database.knowledge.domain.agent.cloudnetwork;

import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

import org.immutables.value.Value;

import java.util.Map;

@JsonSerialize(as = ImmutableCloudNetworkMonitoringData.class)
@JsonDeserialize(as = ImmutableCloudNetworkMonitoringData.class)
@Value.Immutable
public interface CloudNetworkMonitoringData extends NetworkComponentMonitoringData {
	/**
	 * @return map of servers and their weights represented as percentages
	 */
	Map<AID, Double> getPercentagesForServersMap();

	/**
	 * @return available power of the cna
	 */
	double getAvailablePower();
}
