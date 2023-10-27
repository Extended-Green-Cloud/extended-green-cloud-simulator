package org.greencloud.commons.domain.job.basic;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;
import org.immutables.value.internal.$processor$.meta.$CriteriaMirrors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing job that is to be executed in Cloud
 */
@JsonSerialize(as = ImmutablePowerJob.class)
@JsonDeserialize(as = ImmutablePowerJob.class)
@Value.Immutable
@ImmutableConfig
public interface PowerJob extends Serializable {

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return strategy with which the job is to be handled
	 */
	@Nullable
	Integer getStrategyId();

	/**
	 * @return unique job instance identifier
	 */
	@$CriteriaMirrors.CriteriaId
	@Value.Default
	default String getJobInstanceId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * @return time when the power delivery should start
	 */
	Instant getStartTime();

	/**
	 * @return time when the power delivery should finish
	 */
	Instant getEndTime();

	/**
	 * @return time before which job has to end
	 */
	Instant getDeadline();

	/**
	 * @return required amount of resources used to process given job
	 * (resource requirements are defined per single unit of time - per seconds)
	 */
	Map<String, Resource> getRequiredResources();

	/**
	 * @return method returns list of job steps
	 */
	List<JobStep> getJobSteps();
}
