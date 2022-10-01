package com.greencloud.application.domain.job;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object stores the data which allow to identify two instances of the job with the same jobId
 */
@JsonSerialize(as = ImmutableJobInstanceIdentifier.class)
@JsonDeserialize(as = ImmutableJobInstanceIdentifier.class)
@Value.Immutable
public interface JobInstanceIdentifier {

	/**
	 * @return unique job id
	 */
	String getJobId();

	/**
	 * @return job start time
	 */
	Instant getStartTime();
}
