package com.greencloud.commons.args.agent.server;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

@JsonDeserialize(as = ImmutableServerNodeArgs.class)
@JsonSerialize(as = ImmutableServerNodeArgs.class)
@Value.Immutable
public interface ServerNodeArgs extends AgentArgs {

	/**
	 * @return owner cloud network agent name
	 */
	String getCloudNetworkAgent();

	/**
	 * @return energy agents names
	 */
	List<String> getGreenEnergyAgents();

	/**
	 * @return maximum server capacity
	 */
	Double getMaximumCapacity();
}
