package org.greencloud.commons.domain.job.basic;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;
import org.immutables.value.internal.$processor$.meta.$CriteriaMirrors;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.Var;

/**
 * Object storing the data describing job that is to be executed in Cloud
 */
@JsonSerialize(as = ImmutablePowerJob.class)
@JsonDeserialize(as = ImmutablePowerJob.class)
@Value.Style(underrideHashCode = "hash", underrideEquals = "equalTo")
@Value.Immutable
@ImmutableConfig
public interface PowerJob extends Serializable {

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return rule set with which the job is to be handled
	 */
	@Nullable
	Integer getRuleSetId();

	/**
	 * @return unique job instance identifier
	 */
	@$CriteriaMirrors.CriteriaId
	@Value.Default
	default String getJobInstanceId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * @return job execution duration in milliseconds
	 */
	Long getDuration();

	/**
	 * @return time before which job has to end
	 */
	Instant getDeadline();

	/**
	 * @return required amount of resources used to process given job
	 * (resource requirements are defined per single unit of time - per seconds)
	 */
	Map<String, Resource> getRequiredResources();

	/**
	 * @return method returns list of job steps
	 */
	List<JobStep> getJobSteps();

	/**
	 * @return time when the job execution started
	 */
	@Nullable
	Instant getStartTime();

	/**
	 * @return job priority
	 */
	@Nullable
	Integer getPriority();

	/**
	 * @return expected job finish time (if the job has started)
	 */
	@Value.Default
	@Nullable
	default Instant getExpectedEndTime() {
		return ofNullable(getStartTime()).map(startTime -> startTime.plusMillis(getDuration())).orElse(null);
	}

	/**
	 * @return information if job execution is being conducted
	 */
	@Value.Default
	default boolean isUnderExecution() {
		return nonNull(getStartTime());
	}

	default int hash() {
		@Var int h = 5381;
		h += (h << 5) + getJobInstanceId().hashCode();
		return h;
	}

	default boolean equalTo(ImmutablePowerJob another) {
		if (this == another)
			return true;
		return another != null && getJobInstanceId().equals(another.getJobInstanceId());
	}
}
