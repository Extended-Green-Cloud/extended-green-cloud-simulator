package com.greencloud.factory;

import static com.greencloud.commons.args.agent.client.ClientTimeType.SIMULATION;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LATITUDE;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LONGITUDE;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_PRICE;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_TYPE;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_IDLE_POWER;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_JOB_LIMIT;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_MAX_POWER;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static com.greencloud.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_RESOURCES;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.temporal.ValueRange;
import java.util.concurrent.atomic.AtomicInteger;

import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.args.agent.client.ClientTimeType;
import com.greencloud.commons.args.agent.client.ImmutableClientAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.greenenergy.ImmutableGreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.ImmutableMonitoringAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.args.event.newclient.NewClientEventArgs;
import com.greencloud.commons.args.job.JobArgs;
import com.greencloud.commons.domain.resources.HardwareResources;

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
		return createServerAgent(ownerCNA, null, null, null, null, null);
	}

	@Override
	public ServerAgentArgs createServerAgent(String ownerCNA,
			HardwareResources resources,
			Integer maxPower,
			Integer idlePower,
			Integer price,
			Integer jobProcessingLimit) {

		if (isNull(ownerCNA)) {
			throw new IllegalArgumentException("Owner CNA should be specified.");
		}
		if (nonNull(resources) && (resources.getStorage() < 0 || resources.getMemory() < 0 || resources.getCpu() < 0)) {
			throw new IllegalArgumentException("Hardware resources cannot be smaller than 0.");
		}
		if (nonNull(maxPower) && (maxPower < 0)) {
			throw new IllegalArgumentException("Maximum power consumption of the server cannot be smaller than 0.");
		}
		if (nonNull(idlePower) && (idlePower < 0)) {
			throw new IllegalArgumentException("Idle power consumption of the server cannot be smaller than 0.");
		}
		if (nonNull(price) && price < 0) {
			throw new IllegalArgumentException("Price per power unit cannot be smaller than 0.");
		}

		String serverAgentName = "ExtraServer" + serverAgentsCreated.incrementAndGet();

		return ImmutableServerAgentArgs.builder()
				.name(serverAgentName)
				.ownerCloudNetwork(ownerCNA)
				.maxPower(isNull(maxPower) ? TEMPLATE_SERVER_MAX_POWER : maxPower)
				.idlePower(isNull(idlePower) ? TEMPLATE_SERVER_IDLE_POWER : idlePower)
				.price(isNull(price) ? TEMPLATE_SERVER_PRICE : price)
				.resources(isNull(resources) ? TEMPLATE_SERVER_RESOURCES : resources)
				.jobProcessingLimit(isNull(jobProcessingLimit) ? TEMPLATE_SERVER_JOB_LIMIT : jobProcessingLimit)
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
		if (nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity is invalid");
		}
		if (nonNull(pricePerPowerUnit) && pricePerPowerUnit < 0) {
			throw new IllegalArgumentException("pricePerPowerUnit is invalid");
		}
		if (nonNull(latitude) && ValueRange.of(-90, 90).isValidIntValue(latitude)) {
			throw new IllegalArgumentException("latitude is invalid");
		}
		if (nonNull(longitude) && ValueRange.of(-180, 180).isValidIntValue(longitude)) {
			throw new IllegalArgumentException("longitude is invalid");
		}

		String greenEnergyAgentName = "ExtraGreenEnergy" + greenEnergyAgentsCreated.incrementAndGet();
		return ImmutableGreenEnergyAgentArgs.builder()
				.name(greenEnergyAgentName)
				.weatherPredictionError(0.02)
				.monitoringAgent(monitoringAgentName)
				.ownerSever(ownerServerName)
				.latitude(isNull(latitude) ? TEMPLATE_GREEN_ENERGY_LATITUDE : latitude.toString())
				.longitude(isNull(longitude) ? TEMPLATE_GREEN_ENERGY_LONGITUDE : longitude.toString())
				.maximumCapacity(isNull(maximumCapacity) ? TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY : maximumCapacity)
				.weatherPredictionError(isNull(weatherPredictionError) ? 0.0 : weatherPredictionError)
				.pricePerPowerUnit(isNull(pricePerPowerUnit) ? TEMPLATE_GREEN_ENERGY_PRICE : pricePerPowerUnit)
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

	@Override
	public ClientAgentArgs createClientAgent(final String name,
			final String jobId,
			final ClientTimeType timeType,
			final JobArgs clientJob) {
		return ImmutableClientAgentArgs.builder()
				.name(name)
				.jobId(jobId)
				.timeType(timeType)
				.job(clientJob)
				.build();
	}

	@Override
	public ClientAgentArgs createClientAgent(NewClientEventArgs clientEventArgs) {
		return ImmutableClientAgentArgs.builder()
				.name(clientEventArgs.getName())
				.jobId(valueOf(clientEventArgs.getJobId()))
				.job(clientEventArgs.getJob())
				.timeType(SIMULATION)
				.build();
	}
}
