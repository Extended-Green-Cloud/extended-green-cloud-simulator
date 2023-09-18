package com.database.knowledge.domain.agent.client;

import java.util.Map;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.MonitoringData;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.enums.job.JobClientStatusEnum;

/**
 * Interface storing monitoring data sent by the Client Agent
 */
@JsonDeserialize(as = ImmutableClientMonitoringData.class)
@JsonSerialize(as = ImmutableClientMonitoringData.class)
@Value.Immutable
@ImmutableConfig
public interface ClientMonitoringData extends MonitoringData {

	/**
	 * @return flag indicating if the client has left the cloud network
	 */
	boolean getIsFinished();

	/**
	 * @return current status of job execution
	 */
	JobClientStatusEnum getCurrentJobStatus();

	/**
	 * @return time im ms during which the job execution had a certain status
	 */
	Map<JobClientStatusEnum, Long> getJobStatusDurationMap();
}
