package runner.factory;

import java.util.List;
import java.util.Objects;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.args.agent.client.ImmutableClientAgentArgs;
import com.greencloud.commons.args.agent.cloudnetwork.CloudNetworkArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
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

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgentControllerFactoryImpl implements AgentControllerFactory {

	private final ContainerController containerController;

	public AgentControllerFactoryImpl(ContainerController containerController) {
		this.containerController = containerController;
	}

	@Override
	public AgentController createAgentController(AgentArgs agentArgs, ScenarioStructureArgs scenario)
			throws StaleProxyException {

		if (agentArgs instanceof ClientAgentArgs clientAgent) {
			final String startDate = clientAgent.formatClientTime(clientAgent.getStart());
			final String endDate = clientAgent.formatClientTime(clientAgent.getEnd());
			final String deadline = clientAgent.formatClientTime(clientAgent.getDeadline());

			return containerController.createNewAgent(clientAgent.getName(),
					"com.greencloud.application.agents.client.ClientAgent",
					new Object[] { startDate, endDate, deadline, clientAgent.getPower(), clientAgent.getJobId() });
		} else if (agentArgs instanceof ServerAgentArgs serverAgent) {
			return containerController.createNewAgent(serverAgent.getName(),
					"com.greencloud.application.agents.server.ServerAgent",
					new Object[] {
							serverAgent.getOwnerCloudNetwork(),
							serverAgent.getPrice(),
							serverAgent.getMaximumCapacity(),
							serverAgent.getJobProcessingLimit() });
		} else if (agentArgs instanceof CloudNetworkArgs cloudNetworkAgent) {
			return containerController.createNewAgent(cloudNetworkAgent.getName(),
					"com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent", new Object[] {});
		} else if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgent) {
			// TODO add connectedServers() when done
			return containerController.createNewAgent(greenEnergyAgent.getName(),
					"com.greencloud.application.agents.greenenergy.GreenEnergyAgent",
					new Object[] { greenEnergyAgent.getMonitoringAgent(),
							greenEnergyAgent.getOwnerSever(),
							greenEnergyAgent.getMaximumCapacity(),
							greenEnergyAgent.getPricePerPowerUnit(),
							greenEnergyAgent.getLatitude(),
							greenEnergyAgent.getLongitude(),
							greenEnergyAgent.getEnergyType(),
							greenEnergyAgent.getWeatherPredictionError() });
		} else if (agentArgs instanceof MonitoringAgentArgs monitoringAgent) {
			return containerController.createNewAgent(monitoringAgent.getName(),
					"com.greencloud.application.agents.monitoring.MonitoringAgent",
					new Object[] { monitoringAgent.getBadStubProbability() });
		} else if (agentArgs instanceof SchedulerAgentArgs schedulerAgent) {
			return containerController.createNewAgent(agentArgs.getName(),
					"com.greencloud.application.agents.scheduler.SchedulerAgent",
					new Object[] {
							schedulerAgent.getDeadlineWeight(),
							schedulerAgent.getPowerWeight(),
							schedulerAgent.getMaximumQueueSize(),
							schedulerAgent.getJobSplitThreshold(),
							schedulerAgent.getSplittingFactor()
					});
		} else if (agentArgs instanceof ManagingAgentArgs managingAgent) {
			return containerController.createNewAgent(agentArgs.getName(),
					"org.greencloud.managingsystem.agent.ManagingAgent",
					new Object[] {
							managingAgent.getSystemQualityThreshold(),
							scenario,
							containerController,
							managingAgent.getPowerShortageThreshold(),
							managingAgent.getDisabledActions()
					});
		}
		return null;
	}

	@Override
	public AbstractAgentNode createAgentNode(AgentArgs agentArgs, ScenarioStructureArgs scenarioArgs) {
		if (agentArgs instanceof ClientAgentArgs clientArgs) {
			return new ClientAgentNode(ImmutableClientAgentArgs.copyOf(clientArgs)
					.withStart(clientArgs.formatClientTime(clientArgs.getStart()))
					.withEnd(clientArgs.formatClientTime(clientArgs.getEnd()))
					.withDeadline(clientArgs.formatClientTime(clientArgs.getDeadline())));
		}
		if (agentArgs instanceof CloudNetworkArgs cloudNetworkArgs) {
			final List<ServerAgentArgs> ownedServers = scenarioArgs.getServerAgentsArgs().stream()
					.filter(serverArgs -> serverArgs.getOwnerCloudNetwork().equals(cloudNetworkArgs.getName()))
					.toList();
			final double maximumCapacity = ownedServers.stream()
					.mapToDouble(server -> Double.parseDouble(server.getMaximumCapacity())).sum();
			final List<String> serverList = ownedServers.stream().map(ServerAgentArgs::getName).toList();

			return new CloudNetworkAgentNode(cloudNetworkArgs.getName(), maximumCapacity, serverList);
		}
		if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgentArgs) {
			return new GreenEnergyAgentNode(greenEnergyAgentArgs);
		}
		if (agentArgs instanceof MonitoringAgentArgs monitoringAgentArgs) {
			final GreenEnergyAgentArgs ownerGreenSource = scenarioArgs.getGreenEnergyAgentsArgs().stream()
					.filter(greenSourceArgs -> greenSourceArgs.getMonitoringAgent()
							.equals(monitoringAgentArgs.getName()))
					.findFirst()
					.orElse(null);
			if (Objects.nonNull(ownerGreenSource)) {
				return new MonitoringAgentNode(monitoringAgentArgs.getName(), ownerGreenSource.getName());
			}
			return null;
		}
		if (agentArgs instanceof ServerAgentArgs serverAgentArgs) {
			final List<GreenEnergyAgentArgs> ownedGreenSources = scenarioArgs.getGreenEnergyAgentsArgs()
					.stream()
					.filter(greenEnergyArgs -> greenEnergyArgs.getOwnerSever().equals(serverAgentArgs.getName()))
					.toList();
			final List<String> greenSourceNames = ownedGreenSources.stream().map(GreenEnergyAgentArgs::getName)
					.toList();
			return new ServerAgentNode(serverAgentArgs.getName(),
					Double.parseDouble(serverAgentArgs.getMaximumCapacity()), serverAgentArgs.getOwnerCloudNetwork(),
					greenSourceNames);
		}
		if (agentArgs instanceof SchedulerAgentArgs schedulerAgentArgs) {
			return new SchedulerAgentNode(schedulerAgentArgs);
		}
		if (agentArgs instanceof ManagingAgentArgs managingAgentArgs) {
			return new ManagingAgentNode(managingAgentArgs);
		}
		return null;
	}
}
