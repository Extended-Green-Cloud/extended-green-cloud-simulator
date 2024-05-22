package org.greencloud.commons.args.agent.centralmanager.factory;

import java.security.InvalidParameterException;

import org.immutables.value.Value;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build CMA Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCentralManagerArgs.class)
@JsonDeserialize(as = ImmutableCentralManagerArgs.class)
public interface CentralManagerArgs extends AgentArgs {

	/**
	 * @return preferred maximum scheduled job queue size
	 */
	Integer getMaximumQueueSize();

	/**
	 * @return a size of a batch being the number of jobs drawn from the queue
	 */
	Integer getPollingBatchSize();

	@Value.Check
	default void check() {
		if (getMaximumQueueSize() < 1) {
			throw new InvalidParameterException("Maximum queue size must be a positive integer");
		}
		if (getPollingBatchSize() < 1) {
			throw new InvalidParameterException("Polling batch size must be a positive integer");
		}
	}
}
