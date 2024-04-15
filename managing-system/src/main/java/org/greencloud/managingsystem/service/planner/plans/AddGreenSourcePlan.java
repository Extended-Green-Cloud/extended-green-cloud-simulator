package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.max;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;
import java.util.Map;
import java.util.function.DoublePredicate;

import org.greencloud.commons.args.agent.regionalmanager.factory.RegionalManagerArgs;
import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;

import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.adaptation.system.AddGreenSourceActionParameters;

import jade.core.AID;
import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new green source to the system
 */
public class AddGreenSourcePlan extends SystemPlan {

	private static final Double RELATIVE_BACKUP_POWER_VALUE_THRESHOLD = 0.2;
	private static final DoublePredicate isOverThreshold = value -> value >= RELATIVE_BACKUP_POWER_VALUE_THRESHOLD;

	private Map<String, Double> serversData;

	public AddGreenSourcePlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(ADD_GREEN_SOURCE, managingAgent, violatedGoal);
		serversData = emptyMap();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. any server in the system was using back-up power over specified relative back-up power threshold
	 * 2. the considered servers have not been disabled/are not being disabled
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getServersData();
		return serversData.entrySet().stream().anyMatch(entry -> isOverThreshold.test(entry.getValue()));
	}

	/**
	 * Method constructs plan which finds a server with the largest backup power and adds to it a new green source
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final String targetServer = max(serversData.entrySet(), comparingDouble(Map.Entry::getValue)).getKey();

		if (isNull(targetServer)) {
			return null;
		}

		final ServerArgs targetServerArgs = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs().stream()
				.filter(serverAgentArgs -> targetServer.contains(serverAgentArgs.getName()))
				.findFirst()
				.orElse(null);

		if (isNull(targetServerArgs)) {
			return null;
		}

		final String targetRegionalManagerAgent = targetServerArgs.getOwnerRegionalManager();
		final RegionalManagerArgs regionalManager = managingAgent.getGreenCloudStructure().getRegionalManagerAgentsArgs()
				.stream()
				.filter(rma -> rma.getName().equals(targetRegionalManagerAgent))
				.findFirst()
				.orElse(null);

		if (isNull(regionalManager)) {
			return null;
		}

		final String regionalManagerLocation = defaultIfNull(regionalManager.getLocationId(),
				targetServerArgs.getOwnerRegionalManager());
		final MonitoringArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		final GreenEnergyArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), targetServerArgs.getName());
		final Map.Entry<Location, AID> targetLocation = managingAgent.move()
				.findTargetLocation(regionalManagerLocation);

		if (isNull(targetLocation)) {
			return null;
		}

		adaptationPlanInformer = extraGreenEnergyArguments.getName();
		actionParameters = new AddGreenSourceActionParameters(extraGreenEnergyArguments, extraMonitoringAgentArguments,
				targetLocation.getKey(), targetLocation.getValue());

		return this;
	}

	private Map<String, Double> getServersData() {
		final List<AgentData> activeServers = managingAgent.monitor().getActiveServersData();
		return managingAgent.monitor()
				.concatLatestAgentDataWithNotRegistered(activeServers, SERVER, this::getBackUpPowerForServer, 0.0);
	}

	private Double getBackUpPowerForServer(final AgentData dataPerServer) {
		final ServerMonitoringData data = (ServerMonitoringData) dataPerServer.monitoringData();
		return data.getCurrentBackUpPowerTraffic() / (data.getCurrentTraffic() + data.getCurrentBackUpPowerTraffic());

	}
}
