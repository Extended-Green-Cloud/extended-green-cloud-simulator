package org.greencloud.commons.args.job;

import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.Map;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments for the single client Job Step
 */
@JsonSerialize(as = ImmutableSyntheticJobStepArgs.class)
@JsonDeserialize(as = ImmutableSyntheticJobStepArgs.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface SyntheticJobStepArgs {

	/**
	 * @return name of the step
	 */
	String getName();

	/**
	 * @return resources required for job step execution
	 */
	Map<String, Long> getResources();

	/**
	 * @return step execution duration (in seconds, for entire step)
	 */
	Long getDuration();

	/**
	 * Method verifies the correctness of job step structure
	 */
	@Value.Check
	default void check() {
		if (!getResources().containsKey(CPU) || getResources().get(CPU) < 0) {
			throw new InvalidScenarioEventStructure(
					"Given job step is invalid. The job step must specify CPU requirement that is at least equal to 1");
		}
	}
}
