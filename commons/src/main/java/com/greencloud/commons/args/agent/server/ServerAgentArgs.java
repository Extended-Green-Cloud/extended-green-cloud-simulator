package com.greencloud.commons.args.agent.server;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

/**
 * Arguments of the Server Agent
 */
@JsonSerialize(as = ImmutableServerAgentArgs.class)
@JsonDeserialize(as = ImmutableServerAgentArgs.class)
@Immutable
public interface ServerAgentArgs extends AgentArgs {

	/**
	 * @return owner cloud network agent name
	 */
	String getOwnerCloudNetwork();

	/**
	 * @return maximum server capacity
	 */
	String getMaximumCapacity();

	/**
	 * @return price per 1-hour
	 */
	String getPrice();

	/**
	 * @return location's latitude
	 */
	@Nullable
	String getLatitude();

	/**
	 * @return location's longitude
	 */
	@Nullable
	String getLongitude();

}
