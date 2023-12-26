package com.database.knowledge.domain.agent.regionalmanager;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.MonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRegionalManagerMonitoringData.class)
@JsonDeserialize(as = ImmutableRegionalManagerMonitoringData.class)
@Value.Immutable
public interface RegionalManagerMonitoringData extends MonitoringData {

	/**
	 * @return current aggregated success ratio
	 */
	double getSuccessRatio();
}
