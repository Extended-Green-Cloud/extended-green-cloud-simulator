package com.database.knowledge.domain.agent;

import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.job.JobStatusEnum;

/**
 * Interface storing monitoring data sent by the Client Agent
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableClientMonitoringData.class)
@JsonSerialize(as = ImmutableClientMonitoringData.class)
public interface ClientMonitoringData extends MonitoringData {

	/**
	 * @return flag indicating if the client has left the cloud network
	 */
	boolean getIsFinished();

	/**
	 * @return current status of job execution
	 */
	JobStatusEnum getCurrentJobStatus();

	/**
	 * @return time im ms during which the job execution had a certain status
	 */
	Map<JobStatusEnum, Long> getJobStatusDurationMap();
}
