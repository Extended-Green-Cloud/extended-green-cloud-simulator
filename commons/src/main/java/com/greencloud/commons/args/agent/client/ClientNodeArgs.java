package com.greencloud.commons.args.agent.client;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.job.JobStepArgs;

@JsonSerialize(as = ImmutableClientNodeArgs.class)
@JsonDeserialize(as = ImmutableClientNodeArgs.class)
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
	 * @return estimated CPU for the job execution
	 */
	Long getCpu();

	/**
	 * @return estimated memory for the job execution
	 */
	Long getMemory();

	/**
	 * @return estimated storage for the job execution
	 */
	Long getStorage();

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
	 * @return estimated job duration
	 */
	Long getDuration();

	/**
	 * @return list of job steps
	 */
	List<JobStepArgs> getSteps();

}
