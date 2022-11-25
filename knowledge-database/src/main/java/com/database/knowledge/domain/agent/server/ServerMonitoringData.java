package com.database.knowledge.domain.agent.server;

import java.util.Map;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

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
	 * @return maximum number of jobs that can be processed at the given moment
	 */
	int getJobProcessingLimit();

	/**
	 * @return number of jobs being currently executed
	 */
	int getCurrentlyExecutedJobs();

	/**
	 * @return number of jobs being currently processed
	 */
	int getCurrentlyProcessedJobs();

	/**
	 * @return current server traffic
	 */
	double getCurrentTraffic();

	/**
	 * @return price of job execution per hour
	 */
	double getServerPricePerHour();

	/**
	 * @return weights assigned to green source prioritizing their selection
	 */
	Map<AID, Integer> getWeightsForGreenSources();
}
