package com.greencloud.factory;

import static java.lang.Double.parseDouble;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.client.node.ClientNodeArgs;
import org.greencloud.commons.args.agent.client.node.ImmutableClientNodeArgs;
import org.greencloud.commons.args.agent.cloudnetwork.factory.CloudNetworkArgs;
import org.greencloud.commons.args.agent.cloudnetwork.node.CloudNetworkNodeArgs;
import org.greencloud.commons.args.agent.cloudnetwork.node.ImmutableCloudNetworkNodeArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.greenenergy.node.GreenEnergyNodeArgs;
import org.greencloud.commons.args.agent.greenenergy.node.ImmutableGreenEnergyNodeArgs;
import org.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.monitoring.node.ImmutableMonitoringNodeArgs;
import org.greencloud.commons.args.agent.monitoring.node.MonitoringNodeArgs;
import org.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import org.greencloud.commons.args.agent.scheduler.node.ImmutableSchedulerNodeArgs;
import org.greencloud.commons.args.agent.scheduler.node.SchedulerNodeArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.agent.server.node.ImmutableServerNodeArgs;
import org.greencloud.commons.args.agent.server.node.ServerNodeArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.domain.location.ImmutableLocation;
import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.domain.resources.Resource;

import com.gui.agents.client.ClientNode;
import com.gui.agents.cloudnetwork.CloudNetworkNode;
import com.gui.agents.egcs.EGCSNode;
import com.gui.agents.greenenergy.GreenEnergyNode;
import com.gui.agents.managing.ManagingAgentNode;
import com.gui.agents.monitoring.MonitoringNode;
import com.gui.agents.scheduler.SchedulerNode;
import com.gui.agents.server.ServerNode;

public class AgentNodeFactoryImpl implements AgentNodeFactory {

	@Override
	public EGCSNode<?, ?> createAgentNode(final AgentArgs agentArgs, final ScenarioStructureArgs scenarioArgs) {
		if (agentArgs instanceof ClientArgs clientArgs) {
			return createClientNode(clientArgs);
		}
		if (agentArgs instanceof CloudNetworkArgs cloudNetworkArgs) {
			return createCloudNetworkNode(cloudNetworkArgs, scenarioArgs);
		}
		if (agentArgs instanceof GreenEnergyArgs greenEnergyAgentArgs) {
			return createGreenEnergyNode(greenEnergyAgentArgs);
		}
		if (agentArgs instanceof MonitoringArgs monitoringAgentArgs) {
			return createMonitoringNode(monitoringAgentArgs, scenarioArgs);
		}
		if (agentArgs instanceof ServerArgs serverAgentArgs) {
			return createServerNode(serverAgentArgs, scenarioArgs);
		}
		if (agentArgs instanceof SchedulerArgs schedulerAgentArgs) {
			return createSchedulerNode(schedulerAgentArgs);
		}
		if (agentArgs instanceof ManagingAgentArgs managingAgentArgs) {
			return new ManagingAgentNode(managingAgentArgs);
		}
		return null;
	}

	private ClientNode createClientNode(final ClientArgs clientArgs) {
		final ClientNodeArgs nodeArgs = ImmutableClientNodeArgs.builder()
				.name(clientArgs.getName())
				.jobId(clientArgs.getJobId())
				.processorName(clientArgs.getJob().processType())
				.resources(clientArgs.getJob().getResources())
				.start(clientArgs.formatClientTime(0))
				.end(clientArgs.formatClientTime(clientArgs.getJob().getDuration()))
				.deadline(clientArgs.formatClientDeadline())
				.duration(clientArgs.getJob().getDuration())
				.steps(clientArgs.getJob().getJobSteps())
				.build();

		return new ClientNode(nodeArgs);
	}

	private CloudNetworkNode createCloudNetworkNode(final CloudNetworkArgs cloudNetworkArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<ServerArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
				.filter(serverArgs -> serverArgs.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
				.toList();
		final List<String> serverList = ownedServers.stream().map(ServerArgs::getName).toList();
		final CloudNetworkNodeArgs nodeArgs = ImmutableCloudNetworkNodeArgs.builder()
				.serverAgents(serverList)
				.maxServerCpu(getMaxCpu(ownedServers))
				.name(cloudNetworkArgs.getName())
				.build();

		return new CloudNetworkNode(nodeArgs);
	}

	private ServerNode createServerNode(final ServerArgs serverAgentArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<GreenEnergyArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs()
				.stream()
				.filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
				.toList();
		final List<String> greenSourceNames = ownedGreenSources.stream().map(GreenEnergyArgs::getName)
				.toList();
		final Map<String, Resource> emptyResources = serverAgentArgs.getResources().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getEmptyResource()));
		final ServerNodeArgs nodeArgs = ImmutableServerNodeArgs.builder()
				.name(serverAgentArgs.getName())
				.cloudNetworkAgent(serverAgentArgs.getOwnerCloudNetwork())
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
		final Location location = new ImmutableLocation(parseDouble(greenEnergyAgentArgs.getLatitude()),
				parseDouble(greenEnergyAgentArgs.getLongitude()));
		final GreenEnergyNodeArgs nodeArgs = ImmutableGreenEnergyNodeArgs.builder()
				.monitoringAgent(greenEnergyAgentArgs.getMonitoringAgent())
				.serverAgent(greenEnergyAgentArgs.getOwnerSever())
				.maximumCapacity(greenEnergyAgentArgs.getMaximumCapacity())
				.name(greenEnergyAgentArgs.getName())
				.agentLocation(location)
				.energyType(greenEnergyAgentArgs.getEnergyType().name())
				.pricePerPower(greenEnergyAgentArgs.getPricePerPowerUnit())
				.weatherPredictionError(greenEnergyAgentArgs.getWeatherPredictionError() * 100)
				.build();

		return new GreenEnergyNode(nodeArgs);
	}

	private SchedulerNode createSchedulerNode(final SchedulerArgs schedulerArgs) {
		final double deadlinePriority = (double) schedulerArgs.getDeadlineWeight() / (schedulerArgs.getDeadlineWeight()
				+ schedulerArgs.getCpuWeight());
		final double cpuPriority = (double) schedulerArgs.getCpuWeight() / (schedulerArgs.getCpuWeight()
				+ schedulerArgs.getDeadlineWeight());

		final SchedulerNodeArgs nodeArgs = ImmutableSchedulerNodeArgs.builder()
				.name(schedulerArgs.getName())
				.cpuPriority(cpuPriority)
				.deadlinePriority(deadlinePriority)
				.maxQueueSize(schedulerArgs.getMaximumQueueSize())
				.build();

		return new SchedulerNode(nodeArgs);
	}

	private MonitoringNode createMonitoringNode(final MonitoringArgs monitoringArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final GreenEnergyArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
				.filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent().equals(monitoringArgs.getName()))
				.findFirst()
				.orElse(null);
		if (nonNull(ownerGreenSource)) {
			final MonitoringNodeArgs nodeArgs = ImmutableMonitoringNodeArgs.builder()
					.name(monitoringArgs.getName())
					.greenEnergyAgent(ownerGreenSource.getName())
					.build();
			return new MonitoringNode(nodeArgs);
		}
		return null;
	}

	private double getMaxCpu(List<ServerArgs> ownedServers) {
		return ownedServers.stream().mapToDouble(server -> server.getResources().get(CPU).getAmount()).sum();
	}
}
