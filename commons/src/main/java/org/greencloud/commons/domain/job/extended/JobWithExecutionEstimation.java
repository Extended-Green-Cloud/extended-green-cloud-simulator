package org.greencloud.commons.domain.job.extended;

import java.time.Instant;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing job execution estimation
 */
@JsonSerialize(as = ImmutableJobWithExecutionEstimation.class)
@JsonDeserialize(as = ImmutableJobWithExecutionEstimation.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithExecutionEstimation {

	Long getEstimatedDuration();

	Double getEstimatedPrice();

	@Nullable
	Instant getEarliestStartTime();
}
