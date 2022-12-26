package com.database.knowledge.domain.agent.server;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Monitoring data representing the current state of the Server Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerMonitoringData.class)
@JsonDeserialize(as = ImmutableServerMonitoringData.class)
public interface ServerMonitoringData extends NetworkComponentMonitoringData {

	/**
	 * @return maximum capacity of the server at the given moment
	 */
	int getCurrentMaximumCapacity();

	/**
	 * @return current server traffic
	 */
	double getCurrentTraffic();

	/**
	 * @return current back up power usage
	 */
	double getCurrentBackUpPowerUsage();

	/**
	 * @return information wether serve is disabled
	 */
	boolean isDisabled();
}
