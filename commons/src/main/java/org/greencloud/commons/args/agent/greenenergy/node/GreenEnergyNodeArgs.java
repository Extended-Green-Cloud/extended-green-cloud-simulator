package org.greencloud.commons.args.agent.greenenergy.node;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.domain.location.Location;

/**
 * Arguments of the GUI node of green energy agent
 */
@JsonDeserialize(as = ImmutableGreenEnergyNodeArgs.class)
@JsonSerialize(as = ImmutableGreenEnergyNodeArgs.class)
@Value.Immutable
public interface GreenEnergyNodeArgs extends AgentArgs {

	/**
	 * @return owned monitoring agent name
	 */
	String getMonitoringAgent();

	/**
	 * @return owner server name
	 */
	String getServerAgent();

	/**
	 * @return agent location
	 */
	Location getAgentLocation();

	/**
	 * @return maximum capacity of the server
	 */
	Long getMaximumCapacity();

	/**
	 * @return price for energy
	 */
	Long getPricePerPower();

	/**
	 * @return type of energy source
	 */
	String getEnergyType();

	/**
	 * @return weather prediction error
	 */
	double getWeatherPredictionError();
}
