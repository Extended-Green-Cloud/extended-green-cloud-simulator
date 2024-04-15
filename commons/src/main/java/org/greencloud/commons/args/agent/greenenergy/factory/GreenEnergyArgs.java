package org.greencloud.commons.args.agent.greenenergy.factory;

import static java.util.Collections.singleton;

import java.util.ArrayList;

import org.jrba.agentmodel.domain.args.AgentArgs;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Green Energy Agent
 */
@JsonSerialize(as = ImmutableGreenEnergyArgs.class)
@JsonDeserialize(as = ImmutableGreenEnergyArgs.class)
@Value.Immutable
public interface GreenEnergyArgs extends AgentArgs {

	/**
	 * @return owned monitoring agent name
	 */
	String getMonitoringAgent();

	/**
	 * @return owner server name
	 */
	String getOwnerSever();

	/**
	 * @return list of connected servers
	 */
	@Value.Default
	default ArrayList<String> getConnectedServers() {
		return new ArrayList<>(singleton(getOwnerSever()));
	}

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
	Long getPricePerPowerUnit();

	/**
	 * @return initial weather prediction error
	 */
	Double getWeatherPredictionError();

	/**
	 * @return maximum generator capacity
	 */
	Long getMaximumCapacity();

	/**
	 * @return type of energy source
	 */
	GreenEnergySourceTypeEnum getEnergyType();
}
