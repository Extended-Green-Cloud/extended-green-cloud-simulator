package com.database.knowledge.domain.agent;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface storing monitoring data common for network components
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNetworkComponentMonitoringData.class)
@JsonDeserialize(as = ImmutableNetworkComponentMonitoringData.class)
public interface NetworkComponentMonitoringData extends MonitoringData {

	/**
	 * @return current aggregated success ratio
	 */
	double getSuccessRatio();
}
