package org.greencloud.commons.args.job;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments for the single client Job Step
 */
@JsonSerialize(as = ImmutableJobStepArgs.class)
@JsonDeserialize(as = ImmutableJobStepArgs.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface JobStepArgs {

	/**
	 * @return name of the step
	 */
	String getName();

	/**
	 * @return required amount of CPU (im millicores, for entire step)
	 */
	Long getCpu();

	/**
	 * @return required memory (in Mi, for entire step)
	 */
	Long getMemory();

	/**
	 * @return step execution duration (in seconds, for entire step)
	 */
	Long getDuration();

	/**
	 * Method verifies the correctness of job step structure
	 */
	@Value.Check
	default void check() {
		if (getCpu() < 1) {
			throw new InvalidScenarioEventStructure("Given job step is invalid. The cpu must be at least equal to 1");
		}
		if (getMemory() < 1) {
			throw new InvalidScenarioEventStructure(
					"Given job step is invalid. The memory must be at least equal to 1");
		}
	}
}
