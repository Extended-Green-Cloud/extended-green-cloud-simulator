package com.greencloud.factory;

import static java.lang.Double.parseDouble;
import static java.util.Objects.nonNull;

import java.util.List;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.client.factory.ClientArgs;
import com.greencloud.commons.args.agent.client.node.ClientNodeArgs;
import com.greencloud.commons.args.agent.client.node.ImmutableClientNodeArgs;
import com.greencloud.commons.args.agent.cloudnetwork.factory.CloudNetworkArgs;
import com.greencloud.commons.args.agent.cloudnetwork.node.CloudNetworkNodeArgs;
import com.greencloud.commons.args.agent.cloudnetwork.node.ImmutableCloudNetworkNodeArgs;
import com.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import com.greencloud.commons.args.agent.greenenergy.node.GreenEnergyNodeArgs;
import com.greencloud.commons.args.agent.greenenergy.node.ImmutableGreenEnergyNodeArgs;
import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import com.greencloud.commons.args.agent.monitoring.node.ImmutableMonitoringNodeArgs;
import com.greencloud.commons.args.agent.monitoring.node.MonitoringNodeArgs;
import com.greencloud.commons.args.agent.scheduler.factory.SchedulerArgs;
import com.greencloud.commons.args.agent.scheduler.node.ImmutableSchedulerNodeArgs;
import com.greencloud.commons.args.agent.scheduler.node.SchedulerNodeArgs;
import com.greencloud.commons.args.agent.server.factory.ServerArgs;
import com.greencloud.commons.args.agent.server.node.ImmutableServerNodeArgs;
import com.greencloud.commons.args.agent.server.node.ServerNodeArgs;
import com.greencloud.commons.domain.location.ImmutableLocation;
import com.greencloud.commons.domain.location.Location;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.AbstractNode;
import com.gui.agents.ClientNode;
import com.gui.agents.CloudNetworkNode;
import com.gui.agents.GreenEnergyNode;
import com.gui.agents.ManagingAgentNode;
import com.gui.agents.MonitoringNode;
import com.gui.agents.SchedulerNode;
import com.gui.agents.ServerNode;

public class AgentNodeFactoryImpl implements AgentNodeFactory {

	@Override
	public AbstractNode<?, ?> createAgentNode(final AgentArgs agentArgs, final ScenarioStructureArgs scenarioArgs) {
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
				.cpu(clientArgs.getJob().getCpu())
				.memory(clientArgs.getJob().getMemory())
				.storage(clientArgs.getJob().getStorage())
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
		final ServerNodeArgs nodeArgs = ImmutableServerNodeArgs.builder()
				.name(serverAgentArgs.getName())
				.cloudNetworkAgent(serverAgentArgs.getOwnerCloudNetwork())
				.greenEnergyAgents(greenSourceNames)
				.maxPower((long) serverAgentArgs.getMaxPower())
				.idlePower((long) serverAgentArgs.getIdlePower())
				.cpu(serverAgentArgs.getResources().getCpu().longValue())
				.memory(serverAgentArgs.getResources().getMemory().longValue())
				.storage(serverAgentArgs.getResources().getStorage().longValue())
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
		return ownedServers.stream().mapToDouble(server -> server.getResources().getCpu()).sum();
	}
}
