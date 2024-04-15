package org.greencloud.commons.args.agent.scheduler.factory;

import java.security.InvalidParameterException;

import org.greencloud.commons.utils.math.MathOperations;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jrba.agentmodel.domain.args.AgentArgs;

/**
 * Arguments used to build Scheduler Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSchedulerArgs.class)
@JsonDeserialize(as = ImmutableSchedulerArgs.class)
public interface SchedulerArgs extends AgentArgs {

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
		if (!MathOperations.isFibonacci(getDeadlineWeight())) {
			throw new InvalidParameterException("Deadline weight must be an integer from a Fibonacci sequence");
		}
		if (!MathOperations.isFibonacci(getCpuWeight())) {
			throw new InvalidParameterException("Power weight must be an integer from a Fibonacci sequence");
		}
		if (getMaximumQueueSize() < 1) {
			throw new InvalidParameterException("Maximum queue size must be a positive integer");
		}
	}
}
