package org.greencloud.commons.args.job;

import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments for synthetically generated client Job
 */
@JsonSerialize(as = ImmutableSyntheticJobArgs.class)
@JsonDeserialize(as = ImmutableSyntheticJobArgs.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
@Value.Style(jdkOnly = true)
public interface SyntheticJobArgs {

	/**
	 * @return job execution duration (in seconds)
	 */
	Long getDuration();

	/**
	 * @return job execution deadline (in seconds)
	 */
	Long getDeadline();

	/**
	 * @return resources required for job execution
	 */
	Map<String, Long> getResources();

	/**
	 * @return type of process that is to be executed
	 */
	@JsonProperty("processor_name")
	String getProcessorName();

	/**
	 * @return list of partial job steps
	 */
	@JsonProperty("steps")
	List<SyntheticJobStepArgs> getJobSteps();

	@Nullable
	Integer getPriority();

	/**
	 * Method verifies the correctness of job structure
	 */
	@Value.Check
	default void check() {
		if (!getResources().containsKey(CPU) || getResources().get(CPU) < 0) {
			throw new InvalidScenarioEventStructure(
					"Given job is invalid. The job must specify CPU requirement that is at least equal to 1");
		}
	}
}
