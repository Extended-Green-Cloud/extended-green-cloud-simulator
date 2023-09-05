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
	 * @return idle power consumption of given server
	 */
	int getIdlePowerConsumption();

	/**
	 * @return current power consumption of given server
	 */
	double getCurrentPowerConsumption();

	/**
	 * @return current back up power CPU utilization
	 */
	double getCurrentBackUpPowerTraffic();

	/**
	 * @return number of jobs accepted by the server
	 */
	int getServerJobs();

	/**
	 * @return information weather serve is disabled
	 */
	boolean isDisabled();
}
