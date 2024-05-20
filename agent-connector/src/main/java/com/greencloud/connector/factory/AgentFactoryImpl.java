package com.greencloud.connector.factory;

import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_LOCATION;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_PRICE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_GREEN_ENERGY_TYPE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_IDLE_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_JOB_LIMIT;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_MAX_POWER;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_PRICE;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_RESOURCES;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.enums.agent.ClientTimeTypeEnum.REAL_TIME;
import static org.greencloud.commons.enums.agent.ClientTimeTypeEnum.SIMULATION;

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
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.jetbrains.annotations.VisibleForTesting;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AgentFactoryImpl implements AgentFactory {

	private static AtomicInteger serverAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger monitoringAgentsCreated = new AtomicInteger(0);
	private static AtomicInteger greenEnergyAgentsCreated = new AtomicInteger(0);

	@VisibleForTesting
	protected static void reset() {
		serverAgentsCreated = new AtomicInteger(0);
		monitoringAgentsCreated = new AtomicInteger(0);
		greenEnergyAgentsCreated = new AtomicInteger(0);
	}

	@Override
	public ServerArgs createDefaultServerAgent(final String ownerRMA) {
		final String serverAgentName = "ExtraServer" + serverAgentsCreated.incrementAndGet();
		return ImmutableServerArgs.builder()
				.name(serverAgentName)
				.ownerRegionalManager(ownerRMA)
				.maxPower(TEMPLATE_SERVER_MAX_POWER)
				.idlePower(TEMPLATE_SERVER_IDLE_POWER)
				.price(TEMPLATE_SERVER_PRICE)
				.resources(TEMPLATE_SERVER_RESOURCES)
				.jobProcessingLimit(TEMPLATE_SERVER_JOB_LIMIT)
				.build();
	}

	@Override
	public ServerArgs createServerAgent(final ServerCreator serverCreator) {
		return ImmutableServerArgs.builder()
				.name(serverCreator.getName())
				.ownerRegionalManager(serverCreator.getRegionalManager())
				.maxPower(serverCreator.getMaxPower().intValue())
				.idlePower(serverCreator.getIdlePower().intValue())
				.price(serverCreator.getPrice())
				.resources(serverCreator.getResources())
				.jobProcessingLimit(serverCreator.getJobProcessingLimit().intValue())
				.build();
	}

	@Override
	public GreenEnergyArgs createDefaultGreenEnergyAgent(final String monitoringAgentName, final String serverName) {
		final String monitoringName = ofNullable(monitoringAgentName)
				.orElseThrow(() -> new IllegalArgumentException("Name of monitoring agent must be specified"));
		final String ownerServerName = ofNullable(serverName)
				.orElseThrow(() -> new IllegalArgumentException("Name of owner server agent must be specified"));

		final String greenEnergyAgentName = "ExtraGreenEnergy" + greenEnergyAgentsCreated.incrementAndGet();
		return ImmutableGreenEnergyArgs.builder()
				.name(greenEnergyAgentName)
				.monitoringAgent(monitoringName)
				.ownerSever(ownerServerName)
				.location(TEMPLATE_GREEN_ENERGY_LOCATION)
				.maximumCapacity(TEMPLATE_GREEN_ENERGY_MAXIMUM_CAPACITY)
				.weatherPredictionError(0.0)
				.pricePerPowerUnit(TEMPLATE_GREEN_ENERGY_PRICE)
				.energyType(TEMPLATE_GREEN_ENERGY_TYPE)
				.build();
	}

	@Override
	public GreenEnergyArgs createGreenEnergyAgent(final GreenSourceCreator greenSourceCreator,
			final String monitoringName) {
		return ImmutableGreenEnergyArgs.builder()
				.name(greenSourceCreator.getName())
				.weatherPredictionError(greenSourceCreator.getWeatherPredictionError())
				.monitoringAgent(monitoringName)
				.ownerSever(greenSourceCreator.getServer())
				.location(greenSourceCreator.getLocation())
				.maximumCapacity(greenSourceCreator.getMaximumCapacity())
				.pricePerPowerUnit(greenSourceCreator.getPricePerPowerUnit().longValue())
				.energyType(greenSourceCreator.getEnergyType())
				.build();
	}

	@Override
	public MonitoringArgs createDefaultMonitoringAgent() {
		final String monitoringAgentName = "ExtraMonitoring" + monitoringAgentsCreated.incrementAndGet();
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
	public ClientArgs createClientAgent(final NewClientEventArgs clientEventArgs) {
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
				.processorName(jobCreator.getProcessorName())
				.jobSteps(jobCreator.getSteps())
				.deadline(jobCreator.getDeadline() * 60 * 60)
				.duration(jobCreator.getDuration() * 60 * 60)
				.selectionPreference(jobCreator.getSelectionPreference())
				.resources(jobCreator.getResources())
				.priority(1)
				.build();

		return ImmutableClientArgs.builder()
				.name(clientName)
				.job(clientJob)
				.jobId(String.valueOf(nextClientId))
				.timeType(REAL_TIME)
				.build();
	}
}
