package org.greencloud.commons.domain.agent;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data regarding single job estimation
 */
@JsonSerialize(as = ImmutableServerSingleJobEstimation.class)
@JsonDeserialize(as = ImmutableServerSingleJobEstimation.class)
@Value.Immutable
@ImmutableConfig
public interface ServerSingleJobEstimation {

	/**
	 * @return estimation of jobs execution
	 */
	JobWithExecutionEstimation getJobEstimation();

	/**
	 * @return current success ratio of the server
	 */
	Double getServerReliability();

	/**
	 * @return average green energy utilization
	 */
	Double getAverageGreenEnergyUtilization();
}
