package org.greencloud.commons.args.agent.server.factory;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.domain.resources.HardwareResources;

/**
 * Arguments used to build Server Agent
 */
@JsonSerialize(as = ImmutableServerArgs.class)
@JsonDeserialize(as = ImmutableServerArgs.class)
@Immutable
public interface ServerArgs extends AgentArgs {

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
