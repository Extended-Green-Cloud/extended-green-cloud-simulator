package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddServerActionParameters;

import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new server to the system
 */
public class AddServerPlan extends SystemPlan {
	protected static final double TRAFFIC_LOAD_THRESHOLD = 0.8;

	private List<AgentData> serversData;

	public AddServerPlan(ManagingAgent managingAgent) {
		super(ADD_SERVER, managingAgent);
		serversData = emptyList();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. all servers have traffic load over specified constant TRAFFIC_LOAD_THRESHOLD value
	 * (in order to make sure that new servers are not unnecessarily added to the not yet saturated
	 * green cloud network)
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getLastServerData();
		return serversData.stream()
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.mapToDouble(ServerMonitoringData::getCurrentTraffic)
				.allMatch(traffic -> traffic >= TRAFFIC_LOAD_THRESHOLD);
	}

	/**
	 * Method constructs plan which adds the additional server to the system.
	 * The CNA to which new server is added to is selected based on the highest average traffic load.
	 * The method gathers all information necessary to add a server (together with dedicated Green Source
	 * and Monitoring Agent) and passes that information to the ExecutorService.
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final Map<String, Double> cloudNetworkAgentsTraffic = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs().stream()
				.collect(groupingBy(ServerAgentArgs::getOwnerCloudNetwork,
						flatMapping(this::getServerTrafficByName, averagingDouble(Double::doubleValue))));

		if (cloudNetworkAgentsTraffic.isEmpty()) {
			return null;
		}

		final String targetCloudNetworkAgent = max(cloudNetworkAgentsTraffic.entrySet(),
				comparingDouble(Map.Entry::getValue))
				.getKey();

		final ServerAgentArgs extraServerArguments = agentFactory.createDefaultServerAgent(targetCloudNetworkAgent);
		final MonitoringAgentArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		final GreenEnergyAgentArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), extraServerArguments.getName());
		final Location targetLocation = managingAgent.move().findTargetLocation(targetCloudNetworkAgent);

		if (isNull(targetLocation)) {
			return null;
		}

		actionParameters = new AddServerActionParameters(extraServerArguments, extraGreenEnergyArguments,
				extraMonitoringAgentArguments, targetLocation);

		return this;
	}

	private List<AgentData> getLastServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	private Stream<Double> getServerTrafficByName(final ServerAgentArgs serverArgs) {
		final ToDoubleFunction<AgentData> getServerTraffic = data ->
				((ServerMonitoringData) data.monitoringData()).getCurrentTraffic();

		return serversData.stream()
				.filter(server -> server.aid().contains(serverArgs.getName()))
				.map(getServerTraffic::applyAsDouble);
	}
}
