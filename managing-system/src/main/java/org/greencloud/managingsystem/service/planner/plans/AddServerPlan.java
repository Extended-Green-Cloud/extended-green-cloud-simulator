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
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import org.greencloud.commons.args.agent.cloudnetwork.factory.CloudNetworkArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.adaptation.system.AddServerActionParameters;

import jade.core.AID;
import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new server to the system
 */
public class AddServerPlan extends SystemPlan {
	protected static final double TRAFFIC_LOAD_THRESHOLD = 0.4;

	private List<AgentData> serversData;

	public AddServerPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(ADD_SERVER, managingAgent, violatedGoal);
		serversData = emptyList();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. all servers have traffic load over specified constant TRAFFIC_LOAD_THRESHOLD value
	 * (in order to make sure that new servers are not unnecessarily added to the not yet saturated
	 * green cloud network)
	 * 2. there are no servers in the system that has been disabled
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getLastServerData();

		if(serversData.stream().anyMatch(server -> ((ServerMonitoringData)server.monitoringData()).isDisabled())) {
			return false;
		}

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
				.collect(groupingBy(ServerArgs::getOwnerCloudNetwork,
						flatMapping(this::getServerTrafficByName, averagingDouble(Double::doubleValue))));

		if (cloudNetworkAgentsTraffic.isEmpty()) {
			return null;
		}

		final String targetCloudNetworkAgent = max(cloudNetworkAgentsTraffic.entrySet(),
				comparingDouble(Map.Entry::getValue))
				.getKey();

		final CloudNetworkArgs cloudNetwork = managingAgent.getGreenCloudStructure().getCloudNetworkAgentsArgs()
				.stream()
				.filter(cna -> cna.getName().equals(targetCloudNetworkAgent))
				.findFirst()
				.orElse(null);

		if (isNull(cloudNetwork)) {
			return null;
		}

		final String cloudNetworkLocation = defaultIfNull(cloudNetwork.getLocationId(), targetCloudNetworkAgent);
		final ServerArgs extraServerArguments = agentFactory.createDefaultServerAgent(targetCloudNetworkAgent);
		final MonitoringArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		final GreenEnergyArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), extraServerArguments.getName());
		final Map.Entry<Location, AID> targetLocation = managingAgent.move().findTargetLocation(cloudNetworkLocation);

		if (isNull(targetLocation)) {
			return null;
		}

		adaptationPlanInformer = extraServerArguments.getName();
		actionParameters = new AddServerActionParameters(extraServerArguments, extraGreenEnergyArguments,
				extraMonitoringAgentArguments, targetLocation.getKey(), targetLocation.getValue());

		return this;
	}

	private List<AgentData> getLastServerData() {
		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);
	}

	private Stream<Double> getServerTrafficByName(final ServerArgs serverArgs) {
		final ToDoubleFunction<AgentData> getServerTraffic = data ->
				((ServerMonitoringData) data.monitoringData()).getCurrentTraffic();

		return serversData.stream()
				.filter(server -> server.aid().contains(serverArgs.getName()))
				.map(getServerTraffic::applyAsDouble);
	}
}
