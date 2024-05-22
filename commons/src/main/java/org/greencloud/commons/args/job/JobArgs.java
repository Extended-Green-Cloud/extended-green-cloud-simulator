package org.greencloud.commons.args.job;

import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
	String getProcessorName();

	/**
	 * @return list of partial job steps
	 */
	@JsonProperty("steps")
	List<JobStep> getJobSteps();

	@Nullable
	Integer getPriority();

	/**
	 * @return optional server selection preference specified in Expression Language
	 */
	@Nullable
	String getSelectionPreference();

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
