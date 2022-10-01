package com.greencloud.commons.args.greenenergy;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.AgentArgs;

/**
 * Arguments for the Green Source Agent
 */
@JsonSerialize(as = ImmutableGreenEnergyAgentArgs.class)
@JsonDeserialize(as = ImmutableGreenEnergyAgentArgs.class)
@Value.Immutable
public interface GreenEnergyAgentArgs extends AgentArgs {

	/**
	 * @return owned monitoring agent name
	 */
	String getMonitoringAgent();

	/**
	 * @return owner server name
	 */
	String getOwnerSever();

	/**
	 * @return location's latitude
	 */
	String getLatitude();

	/**
	 * @return location's longitude
	 */
	String getLongitude();

	/**
	 * @return price for 1kWh
	 */
	String getPricePerPowerUnit();

	/**
	 * @return maximum capacity of the server
	 */
	String getMaximumCapacity();

	/**
	 * @return type of energy source
	 */
	String getEnergyType();
}
