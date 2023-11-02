package org.greencloud.commons.domain.job.extended;

import java.util.concurrent.atomic.AtomicLong;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.timer.Timer;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing how long job had a given status
 */
@JsonSerialize(as = ImmutableJobStatusWithTime.class)
@JsonDeserialize(as = ImmutableJobStatusWithTime.class)
@Value.Immutable
@ImmutableConfig
public interface JobStatusWithTime {

	AtomicLong getDuration();

	Timer getTimer();
}
