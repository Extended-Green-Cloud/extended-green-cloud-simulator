package org.greencloud.commons.domain.job.extended;

import java.time.Instant;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object stores the information about the time of job status update
 */
@JsonSerialize(as = ImmutableJobWithStatus.class)
@JsonDeserialize(as = ImmutableJobWithStatus.class)
@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JobWithStatus {

	/**
	 * @return job of interest
	 */
	JobInstanceIdentifier getJobInstance();

	/**
	 * @return time of status change
	 */
	Instant getChangeTime();

	/**
	 * @return server executing the job
	 */
	@Nullable
	String getServerName();

	/**
	 * @return cost of execution of the given job
	 */
	@Nullable
	Double getPriceForJob();
}
