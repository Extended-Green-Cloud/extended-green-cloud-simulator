package org.greencloud.commons.args.job;

import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments for the client Job
 */
@JsonSerialize(as = ImmutableJobArgs.class)
@JsonDeserialize(as = ImmutableJobArgs.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
@Value.Style(jdkOnly = true)
public interface JobArgs {

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
	Map<String, Resource> getResources();

	/**
	 * @return type of process that is to be executed
	 */
	@JsonProperty("processor_name")
	String processType();

	/**
	 * @return list of partial job steps
	 */
	@JsonProperty("steps")
	List<JobStep> getJobSteps();

	/**
	 * Method verifies the correctness of job structure
	 */
	@Value.Check
	default void check() {
		if (!getResources().containsKey(CPU) || getResources().get(CPU).getAmount() < 0) {
			throw new InvalidScenarioEventStructure(
					"Given job is invalid. The job must specify CPU requirement that is at least equal to 1");
		}
	}
}
