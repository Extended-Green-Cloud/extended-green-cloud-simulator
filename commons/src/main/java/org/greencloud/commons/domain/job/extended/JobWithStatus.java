package org.greencloud.commons.domain.job.extended;

import java.time.Instant;

import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object stores the information about the time of job status update
 */
@JsonSerialize(as = ImmutableJobWithStatus.class)
@JsonDeserialize(as = ImmutableJobWithStatus.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithStatus {

	/**
	 * @return job of interest
	 */
	JobInstanceIdentifier getJobInstance();

	/**
	 * @return time of status change
	 */
	Instant getChangeTime();
}
