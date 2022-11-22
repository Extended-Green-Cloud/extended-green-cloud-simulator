package com.greencloud.application.agentFactory;

import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LATITUDE;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LONGITUDE;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_PRICE;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_TYPE;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_MAXIMUM_CAPACITY;
import static com.greencloud.application.agentFactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;

import java.time.temporal.ValueRange;
import java.util.Objects;

import com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.ImmutableMonitoringAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

public class AgentFactoryImpl implements AgentFactory {

	private static int serverAgentsCreated = 0;
	private static int monitoringAgentsCreated = 0;
	private static int greenEnergyAgentsCreated = 0;

	public AgentFactoryImpl() {

	}

	public static void reset() {
		serverAgentsCreated = 0;
		monitoringAgentsCreated = 0;
		greenEnergyAgentsCreated = 0;
	}

	@Override
	public ServerAgentArgs createServerAgent(String ownerCNA, Integer maximumCapacity, Integer price,
			Integer jobProcessingLimit) {

		if (Objects.isNull(ownerCNA)) {
			throw new IllegalArgumentException("ownerCna should not be null");
		}
		if (Objects.nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity cannot be null");
		}
		if (Objects.nonNull(price) && price < 0) {
			throw new IllegalArgumentException("price cannot be null");
		}

		serverAgentsCreated += 1;
		String serverAgentName = "ExtraServer" + serverAgentsCreated;

		return ImmutableServerAgentArgs.builder()
				.name(serverAgentName)
				.ownerCloudNetwork(ownerCNA)
				.maximumCapacity(
						Objects.isNull(maximumCapacity) ? TEMPLATE_SERVER_MAXIMUM_CAPACITY : maximumCapacity.toString())
				.price(Objects.isNull(price) ? TEMPLATE_SERVER_PRICE : price.toString())
				.jobProcessingLimit(
						Objects.isNull(jobProcessingLimit) ? TEMPLATE_SERVER_PRICE : jobProcessingLimit.toString())
				.build();
	}

	@Override
	public GreenEnergyAgentArgs createGreenEnergyAgent(
			String monitoringAgentName,
			String ownerServerName,
			Integer latitude,
			Integer longitude,
			Integer maximumCapacity,
			Integer pricePerPowerUnit,
			GreenEnergySourceTypeEnum energyType) {

		if (Objects.isNull(monitoringAgentName) || Objects.isNull(ownerServerName)) {
			throw new IllegalArgumentException("monitoringAgentName and ownerServerName should not be null");
		}
		if (Objects.nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity cannot be null");
		}
		if (Objects.nonNull(pricePerPowerUnit) && pricePerPowerUnit < 0) {
			throw new IllegalArgumentException("pricePerPowerUnit cannot be null");
		}
		if (Objects.nonNull(latitude) && ValueRange.of(-90, 90).isValidIntValue(latitude)) {
			throw new IllegalArgumentException("latitude cannot be null");
		}
		if (Objects.nonNull(longitude) && ValueRange.of(-180, 180).isValidIntValue(longitude)) {
			throw new IllegalArgumentException("longitude cannot be null");
		}

		greenEnergyAgentsCreated += 1;
		String greenEnergyAgentName = "ExtraGreenEnergy" + greenEnergyAgentsCreated;
		return ImmutableGreenEnergyAgentArgs.builder()
				.name(greenEnergyAgentName)
				.monitoringAgent(monitoringAgentName)
				.ownerSever(ownerServerName)
				.latitude(Objects.isNull(latitude) ? TEMPLATE_GREEN_ENERGY_LATITUDE : latitude.toString())
				.longitude(Objects.isNull(longitude) ? TEMPLATE_GREEN_ENERGY_LONGITUDE : longitude.toString())
				.maximumCapacity(Objects.isNull(maximumCapacity) ?
						TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY :
						maximumCapacity.toString())
				.pricePerPowerUnit(
						Objects.isNull(pricePerPowerUnit) ? TEMPLATE_GREEN_ENERGY_PRICE : pricePerPowerUnit.toString())
				.energyType(Objects.isNull(energyType) ? TEMPLATE_GREEN_ENERGY_TYPE : energyType.name())
				.build();
	}

	@Override
	public MonitoringAgentArgs createMonitoringAgent() {
		monitoringAgentsCreated += 1;
		String monitoringAgentName = "ExtraMonitoring" + monitoringAgentsCreated;
		return ImmutableMonitoringAgentArgs.builder()
				.name(monitoringAgentName)
				.build();
	}
}
