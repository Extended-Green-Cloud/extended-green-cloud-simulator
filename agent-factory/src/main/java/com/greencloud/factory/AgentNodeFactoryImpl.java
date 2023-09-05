package com.greencloud.factory;

import static java.util.Objects.nonNull;

import java.util.List;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.args.agent.client.ImmutableClientNodeArgs;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerNodeArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.AbstractAgentNode;
import com.gui.agents.ClientAgentNode;
import com.gui.agents.CloudNetworkAgentNode;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.agents.ManagingAgentNode;
import com.gui.agents.MonitoringAgentNode;
import com.gui.agents.SchedulerAgentNode;
import com.gui.agents.ServerAgentNode;

public class AgentNodeFactoryImpl implements AgentNodeFactory {

	@Override
	public AbstractAgentNode createAgentNode(final AgentArgs agentArgs, final ScenarioStructureArgs scenarioArgs) {
		if (agentArgs instanceof ClientAgentArgs clientArgs) {
			return createClientNode(clientArgs);
		}
		if (agentArgs instanceof CloudNetworkArgs cloudNetworkArgs) {
			return createCloudNetworkNode(cloudNetworkArgs, scenarioArgs);
		}
		if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgentArgs) {
			return new GreenEnergyAgentNode(greenEnergyAgentArgs);
		}
		if (agentArgs instanceof MonitoringAgentArgs monitoringAgentArgs) {
			return createMonitoringNode(monitoringAgentArgs, scenarioArgs);
		}
		if (agentArgs instanceof ServerAgentArgs serverAgentArgs) {
			return createServerNode(serverAgentArgs, scenarioArgs);
		}
		if (agentArgs instanceof SchedulerAgentArgs schedulerAgentArgs) {
			return new SchedulerAgentNode(schedulerAgentArgs);
		}
		if (agentArgs instanceof ManagingAgentArgs managingAgentArgs) {
			return new ManagingAgentNode(managingAgentArgs);
		}
		return null;
	}

	private AbstractAgentNode createClientNode(final ClientAgentArgs clientArgs) {
		return new ClientAgentNode(ImmutableClientNodeArgs.builder()
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
				.build());
	}

	private AbstractAgentNode createCloudNetworkNode(final CloudNetworkArgs cloudNetworkArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<ServerAgentArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
				.filter(serverArgs -> serverArgs.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
				.toList();
		final List<String> serverList = ownedServers.stream().map(ServerAgentArgs::getName).toList();

		return new CloudNetworkAgentNode(cloudNetworkArgs.getName(), getMaxCpu(ownedServers), serverList);
	}

	private AbstractAgentNode createServerNode(final ServerAgentArgs serverAgentArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final List<GreenEnergyAgentArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs()
				.stream()
				.filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
				.toList();
		final List<String> greenSourceNames = ownedGreenSources.stream().map(GreenEnergyAgentArgs::getName)
				.toList();
		return new ServerAgentNode(ImmutableServerNodeArgs.builder()
				.name(serverAgentArgs.getName())
				.cloudNetworkAgent(serverAgentArgs.getOwnerCloudNetwork())
				.greenEnergyAgents(greenSourceNames)
				.maxPower((long) serverAgentArgs.getMaxPower())
				.idlePower((long) serverAgentArgs.getIdlePower())
				.cpu(serverAgentArgs.getResources().getCpu().longValue())
				.memory(serverAgentArgs.getResources().getMemory().longValue())
				.storage(serverAgentArgs.getResources().getStorage().longValue())
				.price(serverAgentArgs.getPrice())
				.build());
	}

	private AbstractAgentNode createMonitoringNode(final MonitoringAgentArgs monitoringArgs,
			final ScenarioStructureArgs scenarioArgs) {
		final GreenEnergyAgentArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
				.filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent().equals(monitoringArgs.getName()))
				.findFirst()
				.orElse(null);
		if (nonNull(ownerGreenSource)) {
			return new MonitoringAgentNode(monitoringArgs.getName(), ownerGreenSource.getName());
		}
		return null;
	}

	private double getMaxCpu(List<ServerAgentArgs> ownedServers) {
		return ownedServers.stream().mapToDouble(server -> server.getResources().getCpu()).sum();
	}
}
