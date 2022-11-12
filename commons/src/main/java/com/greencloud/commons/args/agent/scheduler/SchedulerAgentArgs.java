package com.greencloud.commons.args.agent.scheduler;

import java.security.InvalidParameterException;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

@Value.Immutable
@JsonSerialize(as = ImmutableSchedulerAgentArgs.class)
@JsonDeserialize(as = ImmutableSchedulerAgentArgs.class)
public interface SchedulerAgentArgs extends AgentArgs {

	/**
	 * @return priority weight of the job deadline property
	 */
	Double getDeadlineWeight();

	/**
	 * @return priority weight of the job power property
	 */
	Double getPowerWeight();

	/**
	 * @return preferred maximum scheduled job queue size
	 */
	Integer getMaximumQueueSize();

	@Value.Check
	default void check() {
		if(getDeadlineWeight() < 0 || getDeadlineWeight() > 1) {
			throw new InvalidParameterException("Deadline weight must be a non negative number from range [0,1]");
		}
		if(getPowerWeight() < 0 || getPowerWeight() > 1) {
			throw new InvalidParameterException("Power weight must be a non negative number from range [0,1]");
		}
		if(getPowerWeight() + getDeadlineWeight() != 1) {
			throw new InvalidParameterException("Sum of weight values must be equal to 1");
		}
		if(getMaximumQueueSize() < 1) {
			throw new InvalidParameterException("Maximum queue size must be a positive integer");
		}
	}
}
