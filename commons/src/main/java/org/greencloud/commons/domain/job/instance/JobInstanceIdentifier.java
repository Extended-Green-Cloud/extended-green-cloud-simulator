package org.greencloud.commons.domain.job.instance;

import java.io.Serializable;
import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object stores the data which allow to identify two instances of the job with the same jobId
 */
@JsonSerialize(as = ImmutableJobInstanceIdentifier.class)
@JsonDeserialize(as = ImmutableJobInstanceIdentifier.class)
@Value.Immutable
@ImmutableConfig
public interface JobInstanceIdentifier extends Serializable {

	/**
	 * @return unique job id
	 */
	String getJobId();

	/**
	 * @return unique identifier of job instance
	 */
	String getJobInstanceId();

	/**
	 * @return job start time
	 */
	Instant getStartTime();
}
