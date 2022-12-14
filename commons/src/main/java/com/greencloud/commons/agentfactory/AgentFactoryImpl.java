package com.greencloud.commons.agentfactory;

import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LATITUDE;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LONGITUDE;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_PRICE;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_TYPE;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_JOB_LIMIT;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_MAXIMUM_CAPACITY;
import static com.greencloud.commons.agentfactory.domain.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static java.util.Objects.isNull;

import java.time.temporal.ValueRange;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.ImmutableMonitoringAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;

public class AgentFactoryImpl implements AgentFactory {

	private static AtomicInteger serverAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger monitoringAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger greenEnergyAgentsCreated = new AtomicInteger(0);

	public AgentFactoryImpl() {

	}

	public static void reset() {
		serverAgentsCreated = new AtomicInteger(0);
		monitoringAgentsCreated = new AtomicInteger(0);
		greenEnergyAgentsCreated = new AtomicInteger(0);
	}

	@Override
	public ServerAgentArgs createDefaultServerAgent(String ownerCNA) {
		return createServerAgent(ownerCNA, null, null, null);
	}

	@Override
	public ServerAgentArgs createServerAgent(String ownerCNA, Integer maximumCapacity, Integer price,
			Integer jobProcessingLimit) {

		if (isNull(ownerCNA)) {
			throw new IllegalArgumentException("ownerCna should not be null");
		}
		if (Objects.nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity is invalid");
		}
		if (Objects.nonNull(price) && price < 0) {
			throw new IllegalArgumentException("price is invalid");
		}

		String serverAgentName = "ExtraServer" + serverAgentsCreated.incrementAndGet();

		return ImmutableServerAgentArgs.builder()
				.name(serverAgentName)
				.ownerCloudNetwork(ownerCNA)
				.maximumCapacity(
						isNull(maximumCapacity) ? TEMPLATE_SERVER_MAXIMUM_CAPACITY : maximumCapacity.toString())
				.price(isNull(price) ? TEMPLATE_SERVER_PRICE : price.toString())
				.jobProcessingLimit(
						isNull(jobProcessingLimit) ? TEMPLATE_SERVER_JOB_LIMIT : jobProcessingLimit.toString())
				.build();
	}

	@Override
	public GreenEnergyAgentArgs createDefaultGreenEnergyAgent(String monitoringAgentName, String ownerServerName) {
		return createGreenEnergyAgent(monitoringAgentName, ownerServerName, null, null, null, null, null, null);
	}

	@Override
	public GreenEnergyAgentArgs createGreenEnergyAgent(
			String monitoringAgentName,
			String ownerServerName,
			Integer latitude,
			Integer longitude,
			Integer maximumCapacity,
			Integer pricePerPowerUnit,
			Double weatherPredictionError,
			GreenEnergySourceTypeEnum energyType) {

		if (isNull(monitoringAgentName) || isNull(ownerServerName)) {
			throw new IllegalArgumentException("monitoringAgentName and ownerServerName should not be null");
		}
		if (Objects.nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity is invalid");
		}
		if (Objects.nonNull(pricePerPowerUnit) && pricePerPowerUnit < 0) {
			throw new IllegalArgumentException("pricePerPowerUnit is invalid");
		}
		if (Objects.nonNull(latitude) && ValueRange.of(-90, 90).isValidIntValue(latitude)) {
			throw new IllegalArgumentException("latitude is invalid");
		}
		if (Objects.nonNull(longitude) && ValueRange.of(-180, 180).isValidIntValue(longitude)) {
			throw new IllegalArgumentException("longitude is invalid");
		}
		if (Objects.nonNull(weatherPredictionError) && weatherPredictionError < 0.0 && weatherPredictionError > 100.0) {
			throw new IllegalArgumentException("weather prediction is invalid");
		}

		String greenEnergyAgentName = "ExtraGreenEnergy" + greenEnergyAgentsCreated.incrementAndGet();
		return ImmutableGreenEnergyAgentArgs.builder()
				.name(greenEnergyAgentName)
				.weatherPredictionError("0.02")
				.monitoringAgent(monitoringAgentName)
				.ownerSever(ownerServerName)
				.latitude(isNull(latitude) ? TEMPLATE_GREEN_ENERGY_LATITUDE : latitude.toString())
				.longitude(isNull(longitude) ? TEMPLATE_GREEN_ENERGY_LONGITUDE : longitude.toString())
				.maximumCapacity(isNull(maximumCapacity) ?
						TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY :
						maximumCapacity.toString())
				.weatherPredictionError(isNull(weatherPredictionError) ? "0.0" : weatherPredictionError.toString())
				.pricePerPowerUnit(
						isNull(pricePerPowerUnit) ? TEMPLATE_GREEN_ENERGY_PRICE : pricePerPowerUnit.toString())
				.energyType(isNull(energyType) ? TEMPLATE_GREEN_ENERGY_TYPE : energyType.name())
				.build();
	}

	@Override
	public MonitoringAgentArgs createMonitoringAgent() {
		String monitoringAgentName = "ExtraMonitoring" + monitoringAgentsCreated.incrementAndGet();
		return ImmutableMonitoringAgentArgs.builder()
				.name(monitoringAgentName)
				.build();
	}
}
