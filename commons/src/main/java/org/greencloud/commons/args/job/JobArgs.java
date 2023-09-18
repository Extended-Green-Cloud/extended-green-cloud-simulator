package org.greencloud.commons.args.job;

import java.util.List;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments for the client Job
 */
@JsonSerialize(as = ImmutableJobArgs.class)
@JsonDeserialize(as = ImmutableJobArgs.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
@Value.Style(jdkOnly = true)
public interface JobArgs {

	/**
	 * @return required amount of CPU (in millicores, for entire job)
	 */
	Long getCpu();

	/**
	 * @return required memory (in Mi, for entire job)
	 */
	Long getMemory();

	/**
	 * @return storage size (in Gi, for entire job)
	 */
	Long getStorage();

	/**
	 * @return job execution duration (in seconds)
	 */
	Long getDuration();

	/**
	 * @return job execution deadline (in seconds)
	 */
	Long getDeadline();

	/**
	 * @return type of process that is to be executed
	 */
	@JsonProperty("processor_name")
	String processType();

	/**
	 * @return list of partial job steps
	 */
	@JsonProperty("steps")
	List<JobStepArgs> getJobSteps();

	/**
	 * Method verifies the correctness of job structure
	 */
	@Value.Check
	default void check() {
		if (getCpu() < 1) {
			throw new InvalidScenarioEventStructure("Given job is invalid. The job cpu must be at least equal to 1");
		}
		if (getMemory() < 1) {
			throw new InvalidScenarioEventStructure("Given job is invalid. The job memory must be at least equal to 1");
		}
		if (getDuration() < 1) {
			throw new InvalidScenarioEventStructure("Given job is invalid. The job must last at least 1 second");
		}
	}
}
