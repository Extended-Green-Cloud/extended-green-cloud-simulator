package com.greencloud.commons.args.agent.scheduler;

import java.security.InvalidParameterException;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

import static com.greencloud.commons.utils.CommonUtils.isFibonacci;

@Value.Immutable
@JsonSerialize(as = ImmutableSchedulerAgentArgs.class)
@JsonDeserialize(as = ImmutableSchedulerAgentArgs.class)
public interface SchedulerAgentArgs extends AgentArgs {

	/**
	 * @return priority weight of the job deadline property
	 */
	Integer getDeadlineWeight();

	/**
	 * @return priority weight of the job power property
	 */
	Integer getCpuWeight();

	/**
	 * @return preferred maximum scheduled job queue size
	 */
	Integer getMaximumQueueSize();


	@Value.Check
	default void check() {
		if (!isFibonacci(getDeadlineWeight())) {
			throw new InvalidParameterException("Deadline weight must be an integer from a Fibonacci sequence");
		}
		if (!isFibonacci(getCpuWeight())) {
			throw new InvalidParameterException("Power weight must be an integer from a Fibonacci sequence");
		}
		if (getMaximumQueueSize() < 1) {
			throw new InvalidParameterException("Maximum queue size must be a positive integer");
		}
	}
}
