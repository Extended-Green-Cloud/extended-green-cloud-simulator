package com.greencloud.commons.job;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing power request send to the green source
 */
@JsonSerialize(as = ImmutablePowerJob.class)
@JsonDeserialize(as = ImmutablePowerJob.class)
@Value.Immutable
public interface PowerJob {

	/**
	 * @return unique job identifier
	 */
	String getJobId();

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
	 * @return power that is to be delivered
	 */
	int getPower();
}
