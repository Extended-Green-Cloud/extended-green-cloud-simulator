package com.greencloud.connector.factory;

import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LATITUDE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LONGITUDE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_PRICE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_TYPE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_IDLE_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_JOB_LIMIT;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_MAX_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_RESOURCES;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.enums.agent.ClientTimeTypeEnum.REAL_TIME;
import static org.greencloud.commons.enums.agent.ClientTimeTypeEnum.SIMULATION;

import java.time.temporal.ValueRange;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.client.factory.ImmutableClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.ImmutableGreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.ImmutableMonitoringArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.event.NewClientEventArgs;
import org.greencloud.commons.args.job.ImmutableJobArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.ServerCreator;

public class AgentFactoryImpl implements AgentFactory {

	private static AtomicInteger serverAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger monitoringAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger greenEnergyAgentsCreated = new AtomicInteger(0);

	public AgentFactoryImpl() {
		// used in tests and agent's mobility
	}

	public static void reset() {
		serverAgentsCreated = new AtomicInteger(0);
		monitoringAgentsCreated = new AtomicInteger(0);
		greenEnergyAgentsCreated = new AtomicInteger(0);
	}

	@Override
	public ServerArgs createDefaultServerAgent(String ownerCNA) {
		return createServerAgent(ownerCNA, null, null, null, null, null);
	}

	@Override
	public ServerArgs createServerAgent(final String ownerCNA, final Map<String, Resource> resources,
			final Integer maxPower, final Integer idlePower, final Double price, final Integer jobProcessingLimit) {
		final String serverAgentName = "ExtraServer" + serverAgentsCreated.incrementAndGet();
		return ImmutableServerArgs.builder()
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
	public ServerArgs createServerAgent(final ServerCreator serverCreator) {
		return ImmutableServerArgs.builder()
				.name(serverCreator.getName())
				.ownerCloudNetwork(serverCreator.getCloudNetwork())
				.maxPower(serverCreator.getMaxPower().intValue())
				.idlePower(serverCreator.getIdlePower().intValue())
				.price(serverCreator.getPrice())
				.resources(serverCreator.getResources())
				.jobProcessingLimit(serverCreator.getJobProcessingLimit().intValue())
				.build();
	}

	@Override
	public GreenEnergyArgs createDefaultGreenEnergyAgent(String monitoringAgentName, String ownerServerName) {
		return createGreenEnergyAgent(monitoringAgentName, ownerServerName, null, null, null, null, null, null);
	}

	@Override
	public GreenEnergyArgs createGreenEnergyAgent(final String monitoringAgentName, final String ownerServerName,
			final Integer latitude, final Integer longitude, final Integer maximumCapacity,
			final Integer pricePerPowerUnit, final Double weatherPredictionError,
			final GreenEnergySourceTypeEnum energyType) {

		if (isNull(monitoringAgentName) || isNull(ownerServerName)) {
			throw new IllegalArgumentException("Name of monitoring agent and owner server must be specified");
		}
		if (nonNull(maximumCapacity) && maximumCapacity < 0) {
			throw new IllegalArgumentException("Maximum capacity value must bre greater than zero");
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
		return ImmutableGreenEnergyArgs.builder()
				.name(greenEnergyAgentName)
				.monitoringAgent(monitoringAgentName)
				.ownerSever(ownerServerName)
				.latitude(isNull(latitude) ? TEMPLATE_GREEN_ENERGY_LATITUDE : latitude.toString())
				.longitude(isNull(longitude) ? TEMPLATE_GREEN_ENERGY_LONGITUDE : longitude.toString())
				.maximumCapacity(
						isNull(maximumCapacity) ? TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY : (long) maximumCapacity)
				.weatherPredictionError(isNull(weatherPredictionError) ? 0.0 : weatherPredictionError)
				.pricePerPowerUnit(isNull(pricePerPowerUnit) ? TEMPLATE_GREEN_ENERGY_PRICE : (long) pricePerPowerUnit)
				.energyType(isNull(energyType) ? TEMPLATE_GREEN_ENERGY_TYPE : energyType)
				.build();
	}


	@Override
	public GreenEnergyArgs createGreenEnergyAgent(final GreenSourceCreator greenSourceCreator, final String monitoringName) {
		return ImmutableGreenEnergyArgs.builder()
				.name(greenSourceCreator.getName())
				.weatherPredictionError(greenSourceCreator.getWeatherPredictionError())
				.monitoringAgent(monitoringName)
				.ownerSever(greenSourceCreator.getServer())
				.latitude(greenSourceCreator.getLatitude().toString())
				.longitude(greenSourceCreator.getLongitude().toString())
				.maximumCapacity(greenSourceCreator.getMaximumCapacity())
				.pricePerPowerUnit(greenSourceCreator.getPricePerPowerUnit().longValue())
				.energyType(greenSourceCreator.getEnergyType())
				.build();
	}

	@Override
	public MonitoringArgs createMonitoringAgent() {
		String monitoringAgentName = "ExtraMonitoring" + monitoringAgentsCreated.incrementAndGet();
		return ImmutableMonitoringArgs.builder()
				.name(monitoringAgentName)
				.build();
	}

	@Override
	public MonitoringArgs createMonitoringAgent(final String name) {
		return ImmutableMonitoringArgs.builder()
				.name(name)
				.build();
	}

	@Override
	public ClientArgs createClientAgent(final String name,
			final String jobId,
			final ClientTimeTypeEnum timeType,
			final JobArgs clientJob) {
		return ImmutableClientArgs.builder()
				.name(name)
				.jobId(jobId)
				.timeType(timeType)
				.job(clientJob)
				.build();
	}

	@Override
	public ClientArgs createClientAgent(NewClientEventArgs clientEventArgs) {
		return ImmutableClientArgs.builder()
				.name(clientEventArgs.getName())
				.jobId(valueOf(clientEventArgs.getJobId()))
				.job(clientEventArgs.getJob())
				.timeType(SIMULATION)
				.build();
	}

	@Override
	public ClientArgs createClientAgent(final JobCreator jobCreator, final String clientName, final int nextClientId) {
		final JobArgs clientJob = ImmutableJobArgs.builder()
				.processType(jobCreator.getProcessorName())
				.jobSteps(jobCreator.getSteps())
				.deadline(jobCreator.getDeadline() * 60 * 60)
				.duration(jobCreator.getDuration() * 60 * 60)
				.selectionPreference(jobCreator.getSelectionPreference())
				.resources(jobCreator.getResources())
				.build();

		return ImmutableClientArgs.builder()
				.name(clientName)
				.job(clientJob)
				.jobId(String.valueOf(nextClientId))
				.timeType(REAL_TIME)
				.build();
	}
}
