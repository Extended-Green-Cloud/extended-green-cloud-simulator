package com.greencloud.connector.factory;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.factory.CentralManagerArgs;
import org.greencloud.commons.args.agent.centralmanager.node.CentralManagerNodeArgs;
import org.greencloud.commons.args.agent.centralmanager.node.ImmutableSchedulerNodeArgs;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.client.node.ClientNodeArgs;
import org.greencloud.commons.args.agent.client.node.ImmutableClientNodeArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.greenenergy.node.GreenEnergyNodeArgs;
import org.greencloud.commons.args.agent.greenenergy.node.ImmutableGreenEnergyNodeArgs;
import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.monitoring.node.ImmutableMonitoringNodeArgs;
import org.greencloud.commons.args.agent.monitoring.node.MonitoringNodeArgs;
import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.commons.args.agent.regionalmanager.node.ImmutableRegionalManagerNodeArgs;
import org.greencloud.commons.args.agent.regionalmanager.node.RegionalManagerNodeArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.agent.server.node.ImmutableServerNodeArgs;
import org.greencloud.commons.args.agent.server.node.ServerNodeArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.client.ClientNode;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.gui.agents.monitoring.MonitoringNode;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.gui.agents.scheduler.CMANode;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.agentmodel.domain.args.AgentArgs;

public class AgentNodeFactoryImpl implements AgentNodeFactory {

	@Override
	public EGCSNode<?, ?> createAgentNode(final AgentArgs agentArgs, final ScenarioStructureArgs scenarioArgs) {
		return switch (agentArgs) {
			case ClientArgs clientArgs -> createClientNode(clientArgs);
			case RegionalManagerArgs rmaArgs -> createRegionalManagerNode(rmaArgs, scenarioArgs);
			case GreenEnergyArgs greenEnergyArgs -> createGreenEnergyNode(greenEnergyArgs);
			case MonitoringArgs monitoringArgs -> createMonitoringNode(monitoringArgs, scenarioArgs);
			case ServerArgs serverArgs -> createServerNode(serverArgs, scenarioArgs);
			case CentralManagerArgs centralManagerArgs -> createCentralManagerNode(centralManagerArgs);
			case ManagingAgentArgs managingAgentArgs -> new ManagingAgentNode(managingAgentArgs);
			default -> null;
		};
	}

	@Override
	public MonitoringNode createMonitoringNode(final MonitoringArgs monitoringArgs, final String greenSourceName) {
		final MonitoringNodeArgs nodeArgs = ImmutableMonitoringNodeArgs.builder()
				.name(monitoringArgs.getName())
				.greenEnergyAgent(greenSourceName)
				.build();
		return new MonitoringNode(nodeArgs);
	}

	@Override
	public ServerNode createServerNode(final ServerArgs serverArgs) {
		final Map<String, Resource> emptyResources = serverArgs.getResources().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEmptyResource()));
		final ServerNodeArgs nodeArgs = ImmutableServerNodeArgs.builder()
				.name(serverArgs.getName())
				.regionalManagerAgent(serverArgs.getOwnerRegionalManager())
				.greenEnergyAgents(new HashSet<>())
				.maxPower((long) serverArgs.getMaxPower())
				.idlePower((long) serverArgs.getIdlePower())
				.resources(serverArgs.getResources())
				.emptyResources(emptyResources)
				.price(serverArgs.getPrice())
				.build();
		return new ServerNode(nodeArgs);
	}

	private ClientNode createClientNode(final ClientArgs clientArgs) {
		final ClientNodeArgs nodeArgs = ImmutableClientNodeArgs.builder()
				.name(clientArgs.getName())
				.jobId(clientArgs.getJobId())
				.processorName(clientArgs.getJob().getProcessorName())
				.resources(clientArgs.getJob().getResources())
				.start(clientArgs.formatClientTime(0))
				.end(clientArgs.formatClientTime(clientArgs.getJob().getDuration()))
				.deadline(clientArgs.formatClientDeadline())
				.duration(clientArgs.getJob().getDuration())
				.steps(clientArgs.getJob().getJobSteps())
				.selectionPreference(clientArgs.getJob().getSelectionPreference())
				.build();

		return new ClientNode(nodeArgs);
	}

	private RegionalManagerNode createRegionalManagerNode(final RegionalManagerArgs regionalManagerArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<ServerArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
				.filter(serverArgs -> serverArgs.getOwnerRegionalManager().equals(regionalManagerArgs.getName()))
				.toList();
		final List<String> serverList = ownedServers.stream().map(ServerArgs::getName).toList();
		final RegionalManagerNodeArgs nodeArgs = ImmutableRegionalManagerNodeArgs.builder()
				.serverAgents(serverList)
				.maxServerCpu(getMaxCpu(ownedServers))
				.ownedResources(new HashMap<>())
				.name(regionalManagerArgs.getName())
				.build();

		return new RegionalManagerNode(nodeArgs);
	}

	private ServerNode createServerNode(final ServerArgs serverAgentArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<GreenEnergyArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs().stream()
				.filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
				.toList();
		final List<String> greenSourceNames = ownedGreenSources.stream().map(GreenEnergyArgs::getName).toList();
		final Map<String, Resource> emptyResources = serverAgentArgs.getResources().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEmptyResource()));
		final ServerNodeArgs nodeArgs = ImmutableServerNodeArgs.builder()
				.name(serverAgentArgs.getName())
				.regionalManagerAgent(serverAgentArgs.getOwnerRegionalManager())
				.greenEnergyAgents(greenSourceNames)
				.maxPower((long) serverAgentArgs.getMaxPower())
				.idlePower((long) serverAgentArgs.getIdlePower())
				.resources(serverAgentArgs.getResources())
				.emptyResources(emptyResources)
				.price(serverAgentArgs.getPrice())
				.build();

		return new ServerNode(nodeArgs);
	}

	private GreenEnergyNode createGreenEnergyNode(final GreenEnergyArgs greenEnergyAgentArgs) {
		final GreenEnergyNodeArgs nodeArgs = ImmutableGreenEnergyNodeArgs.builder()
				.monitoringAgent(greenEnergyAgentArgs.getMonitoringAgent())
				.serverAgent(greenEnergyAgentArgs.getOwnerSever())
				.maximumCapacity(greenEnergyAgentArgs.getMaximumCapacity())
				.name(greenEnergyAgentArgs.getName())
				.agentLocation(greenEnergyAgentArgs.getLocation())
				.energyType(greenEnergyAgentArgs.getEnergyType().name())
				.pricePerPower(greenEnergyAgentArgs.getPricePerPowerUnit())
				.weatherPredictionError(greenEnergyAgentArgs.getWeatherPredictionError() * 100)
				.build();

		return new GreenEnergyNode(nodeArgs);
	}

	private CMANode createCentralManagerNode(final CentralManagerArgs schedulerArgs) {
		final CentralManagerNodeArgs nodeArgs = ImmutableSchedulerNodeArgs.builder()
				.name(schedulerArgs.getName())
				.maxQueueSize(schedulerArgs.getMaximumQueueSize())
				.build();

		return new CMANode(nodeArgs);
	}

	private MonitoringNode createMonitoringNode(final MonitoringArgs monitoringArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final GreenEnergyArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
				.filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent().equals(monitoringArgs.getName()))
				.findFirst()
				.orElse(null);

		return ofNullable(ownerGreenSource)
				.map(args -> new MonitoringNode(ImmutableMonitoringNodeArgs.builder()
						.name(monitoringArgs.getName())
						.greenEnergyAgent(ownerGreenSource.getName())
						.build()))
				.orElse(null);
	}

	private double getMaxCpu(List<ServerArgs> ownedServers) {
		return ownedServers.stream().mapToDouble(server -> server.getResources().get(CPU).getAmount()).sum();
	}
}
