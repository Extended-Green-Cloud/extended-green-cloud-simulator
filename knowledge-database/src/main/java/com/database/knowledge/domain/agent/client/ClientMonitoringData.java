package com.database.knowledge.domain.agent.client;

import java.time.Instant;
import java.util.Map;

import org.immutables.value.Value;

import com.database.knowledge.domain.agent.MonitoringData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.jetbrains.annotations.Nullable;

/**
 * Interface storing monitoring data sent by the Client Agent
 */
@JsonDeserialize(as = ImmutableClientMonitoringData.class)
@JsonSerialize(as = ImmutableClientMonitoringData.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
@ImmutableConfig
public interface ClientMonitoringData extends MonitoringData {

	/**
	 * @return flag indicating if the client has left the cloud network
	 */
	boolean getIsFinished();

	/**
	 * @return flag indicating if the client execution was completed within deadline
	 */
	boolean getIsWithinDeadline();

	/**
	 * @return flag indicating if the client execution was completed within budget
	 */
	boolean getIsWithinBudget();

	/**
	 * @return estimated job execution duration
	 */
	@Nullable
	Long getExpectedExecutionDuration();

	/**
	 * @return estimated job execution time
	 */
	@Nullable
	Instant getEstimatedExecutionEnd();

	/**
	 * @return current status of job execution
	 */
	JobClientStatusEnum getCurrentJobStatus();

	/**
	 * @return time im ms during which the job execution had a certain status
	 */
	Map<JobClientStatusEnum, Long> getJobStatusDurationMap();
}
