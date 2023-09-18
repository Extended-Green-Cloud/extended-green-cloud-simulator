package org.greencloud.commons.domain.job.extended;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object stores the data which allow client to adjust the time frames
 */
@JsonSerialize(as = ImmutableJobWithTimeFrames.class)
@JsonDeserialize(as = ImmutableJobWithTimeFrames.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithTimeFrames {

	/**
	 * @return adjusted job start time
	 */
	Instant getNewJobStart();

	/**
	 * @return adjusted job end time
	 */
	Instant getNewJobEnd();

	/**
	 * @return id of the job, required to handle split jobs
	 */
	String getJobId();
}
