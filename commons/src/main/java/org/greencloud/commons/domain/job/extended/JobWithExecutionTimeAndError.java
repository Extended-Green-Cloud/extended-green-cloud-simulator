package org.greencloud.commons.domain.job.extended;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing the fastest job execution time and potential error in ms.
 */
@JsonSerialize(as = ImmutableJobWithExecutionTimeAndError.class)
@JsonDeserialize(as = ImmutableJobWithExecutionTimeAndError.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithExecutionTimeAndError {

	/**
	 * @return estimated job execution time
	 */
	Long getExecutionTime();

	/**
	 * @return highest error in job execution
	 */
	Long getHighestError();
}
