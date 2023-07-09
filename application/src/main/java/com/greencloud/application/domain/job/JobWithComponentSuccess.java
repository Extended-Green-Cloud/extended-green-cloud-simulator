package com.greencloud.application.domain.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data describing job and the success ratio of the given component
 */
@JsonSerialize(as = ImmutableJobWithComponentSuccess.class)
@JsonDeserialize(as = ImmutableJobWithComponentSuccess.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithComponentSuccess {

	/**
	 * @return unique identifier of the given job
	 */
	String getJobId();

	/**
	 * @return success in job execution of the given component
	 */
	double getSuccessRatio();
}
