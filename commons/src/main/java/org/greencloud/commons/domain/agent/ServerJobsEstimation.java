package org.greencloud.commons.domain.agent;

import java.util.Map;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data regarding jobs estimation
 */
@JsonSerialize(as = ImmutableServerJobsEstimation.class)
@JsonDeserialize(as = ImmutableServerJobsEstimation.class)
@Value.Immutable
@ImmutableConfig
public interface ServerJobsEstimation {

	/**
	 * @return estimation of jobs execution
	 */
	Map<String, JobWithExecutionEstimation> getJobsEstimation();

	/**
	 * @return current success ratio of the server
	 */
	Double getServerReliability();
}
