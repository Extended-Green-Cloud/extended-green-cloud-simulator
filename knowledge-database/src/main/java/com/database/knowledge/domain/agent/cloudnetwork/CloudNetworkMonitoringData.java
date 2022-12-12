package com.database.knowledge.domain.agent.cloudnetwork;

import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

import org.immutables.value.Value;

import java.util.Map;

@JsonSerialize(as = CloudNetworkMonitoringData.class)
@JsonDeserialize(as = CloudNetworkMonitoringData.class)
@Value.Immutable
public interface CloudNetworkMonitoringData extends NetworkComponentMonitoringData {
	/**
	 * @return map of servers and their weights represented as percentages
	 */
	Map<AID, Double> getPercentagesForServersMap();

	/**
	 * @return current traffic of the network
	 */
	double getCurrentTraffic();
}
