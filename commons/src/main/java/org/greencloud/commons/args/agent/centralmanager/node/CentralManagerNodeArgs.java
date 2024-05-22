package org.greencloud.commons.args.agent.centralmanager.node;

import org.immutables.value.Value;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to construct GUI node of Central Manager Agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCentralManagerNodeArgs.class)
@JsonDeserialize(as = ImmutableCentralManagerNodeArgs.class)
public interface CentralManagerNodeArgs extends AgentArgs {

	/**
	 * @return maximal size of central job queue
	 */
	Integer getMaxQueueSize();

}
