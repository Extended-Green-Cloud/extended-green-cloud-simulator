package org.greencloud.managingsystem.service.executor.jade;

import java.util.List;
import java.util.Objects;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.AbstractAgentNode;
import com.gui.agents.GreenEnergyAgentNode;
import com.gui.agents.MonitoringAgentNode;
import com.gui.agents.ServerAgentNode;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class AgentControllerFactory {

	private final ContainerController containerController;

	public AgentControllerFactory(ContainerController containerController) {
		this.containerController = containerController;
	}

	public AgentController createAgentController(AgentArgs agentArgs) throws StaleProxyException {
		if (agentArgs instanceof ServerAgentArgs serverAgent) {
			return containerController.createNewAgent(serverAgent.getName(),
					"com.greencloud.application.agents.server.ServerAgent",
					new Object[] {
							serverAgent.getOwnerCloudNetwork(),
							serverAgent.getPrice(),
							serverAgent.getMaximumCapacity(),
							serverAgent.getJobProcessingLimit() });
		} else if (agentArgs instanceof GreenEnergyAgentArgs greenEnergyAgent) {
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
					new Object[] {});
		}
		return null;
	}

	public AbstractAgentNode createAgentNode(AgentArgs agentArgs, ScenarioStructureArgs scenarioArgs) {
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

		return null;
	}
}
