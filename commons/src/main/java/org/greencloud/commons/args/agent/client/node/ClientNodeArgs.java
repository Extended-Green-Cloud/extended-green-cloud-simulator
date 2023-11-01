package org.greencloud.commons.args.agent.client.node;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to construct GUI node of Client Agent
 */
@JsonSerialize(as = ImmutableClientNodeArgs.class)
@JsonDeserialize(as = ImmutableClientNodeArgs.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Value.Immutable
public interface ClientNodeArgs extends AgentArgs {

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return type of process of the client job
	 */
	String getProcessorName();

	/**
	 * @return estimated resources for the job execution
	 */
	Map<String, Resource> getResources();

	/**
	 * @return estimated job start date
	 */
	String getStart();

	/**
	 * @return estimated job finish date
	 */
	String getEnd();

	/**
	 * @return estimated job deadline
	 */
	String getDeadline();

	/**
	 * @return optional server selection preference specified in Expression Language
	 */
	@Nullable
	String getSelectionPreference();

	/**
	 * @return estimated job duration
	 */
	Long getDuration();

	/**
	 * @return list of job steps
	 */
	List<JobStep> getSteps();

}
