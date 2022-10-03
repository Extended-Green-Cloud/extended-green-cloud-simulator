package com.greencloud.application.domain.job;

import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;

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
public interface PowerJob extends AbstractJob{

	default boolean isExecutedAtTime(Instant timestamp) {
		return isWithinTimeStamp(getStartTime(), getEndTime(), timestamp);
	}
}
