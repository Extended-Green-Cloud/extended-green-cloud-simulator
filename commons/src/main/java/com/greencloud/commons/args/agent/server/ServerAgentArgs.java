package com.greencloud.commons.args.agent.server;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.domain.resources.HardwareResources;

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
	 * @return maximum server power consumption (i.e. when CPU load is 100%)
	 */
	Integer getMaxPower();

	/**
	 * @return idle power consumption
	 */
	Integer getIdlePower();

	/**
	 * @return amount of hardware resources owned by server
	 */
	HardwareResources getResources();

	/**
	 * @return limit of jobs that can be processed at the same time
	 */
	Integer getJobProcessingLimit();

	/**
	 * @return price per 1-hour
	 */
	Double getPrice();

	@Nullable
	String getContainerId();

}
