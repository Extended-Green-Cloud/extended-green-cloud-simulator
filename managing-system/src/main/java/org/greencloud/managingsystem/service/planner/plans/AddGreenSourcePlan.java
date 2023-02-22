package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Collections.emptyMap;
import static java.util.Collections.max;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.ToDoubleFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddGreenSourceActionParameters;

import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new green source to the system
 */
public class AddGreenSourcePlan extends SystemPlan {

	private static final Double RELATIVE_BACKUP_POWER_VALUE_THRESHOLD = 0.2;
	private static final DoublePredicate isOverThreshold = value -> value >= RELATIVE_BACKUP_POWER_VALUE_THRESHOLD;

	private Map<String, List<Double>> serversData;

	public AddGreenSourcePlan(ManagingAgent managingAgent) {
		super(ADD_GREEN_SOURCE, managingAgent);
		serversData = emptyMap();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. any server in the system was using back-up power over specified relative back-up power threshold
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getServerData().stream()
				.collect(groupingBy(AgentData::aid))
				.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, this::getBackUpPowerForServer));
		return serversData.entrySet().stream()
				.anyMatch(entry -> entry.getValue().stream().anyMatch(isOverThreshold::test));
	}

	/**
	 * Method constructs plan which finds a server with the largest backup power and adds to it a new green source
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final String targetServer = max(serversData.entrySet(),
				comparingDouble(entry -> max(entry.getValue()))).getKey();

		if (isNull(targetServer)) {
			return null;
		}

		final ServerAgentArgs targetServerArgs = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs().stream()
				.filter(serverAgentArgs -> targetServer.contains(serverAgentArgs.getName()))
				.findFirst()
				.orElse(null);

		if (isNull(targetServerArgs)) {
			return null;
		}

		final String targetCloudNetworkAgent = targetServerArgs.getOwnerCloudNetwork();
		final MonitoringAgentArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		final GreenEnergyAgentArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), targetServerArgs.getName());
		final Location targetLocation = managingAgent.move().findTargetLocation(targetCloudNetworkAgent);

		if (isNull(targetLocation)) {
			return null;
		}

		actionParameters = new AddGreenSourceActionParameters(extraGreenEnergyArguments, extraMonitoringAgentArguments,
				targetLocation);

		return this;
	}

	private List<AgentData> getServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	private List<Double> getBackUpPowerForServer(final Map.Entry<String, List<AgentData>> dataPerServer) {
		final ToDoubleFunction<ServerMonitoringData> getRelativeBackUpPower = data ->
				data.getCurrentBackUpPowerUsage() / (data.getCurrentTraffic() + data.getCurrentBackUpPowerUsage());

		return dataPerServer.getValue().stream()
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.map(getRelativeBackUpPower::applyAsDouble)
				.toList();
	}
}
