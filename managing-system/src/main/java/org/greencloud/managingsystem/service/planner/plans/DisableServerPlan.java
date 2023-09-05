package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Collections.max;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.ImmutableDisableServerActionParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of disabling the idle server
 */
public class DisableServerPlan extends AbstractPlan {

	private static final double TRAFFIC_THRESHOLD = 0.2;
	private static final double SERVER_JOBS_NO_THRESHOLDS = 15;

	private final Map<String, Integer> idleServers;

	public DisableServerPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(DISABLE_SERVER, managingAgent, violatedGoal);
		idleServers = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some idle servers in the system (i.e. servers which traffic as well as number
	 * of planned jobs is below corresponding thresholds)
	 * 2. those servers haven't been yet disabled
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> serverData = managingAgent.getAgentNode().getDatabaseClient().
				readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

		// get names of servers which average traffic is within given threshold
		final List<String> serversWithTrafficInBounds = getServersWithinTrafficThreshold(serverData);

		// consider servers that comply with all constraints
		idleServers.putAll(serverData.stream()
				.filter(server -> canServerBeDisabled(serversWithTrafficInBounds).test(server))
				.collect(toMap(AgentData::aid,
						data -> ((ServerMonitoringData) data.monitoringData()).getIdlePowerConsumption())));

		// consider servers which has not registered any data in database (i.e. did not accept any jobs)
		idleServers.putAll(getServersNotInDatabase(serverData));

		return !idleServers.isEmpty();
	}

	/**
	 * Method chooses the idle server to disable.
	 * The server is chosen based on the following criteria:
	 * 1. The server with the highest idle power consumption
	 *
	 * @return Constructed plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final String chosenServer = max(idleServers.entrySet(), comparingByValue()).getKey();

		targetAgent = new AID(chosenServer, AID.ISGUID);
		actionParameters = ImmutableDisableServerActionParameters.builder().build();

		return this;
	}

	private List<String> getServersWithinTrafficThreshold(final List<AgentData> agentDataFromDatabase) {
		final List<String> aliveServers = managingAgent.monitor()
				.getAliveAgentsIntersection(SERVER, agentDataFromDatabase.stream().map(AgentData::aid).toList());

		return managingAgent.monitor()
				.getAverageTrafficForNetworkComponent(aliveServers, SERVER_MONITORING).entrySet().stream()
				.filter(entry -> entry.getValue() <= TRAFFIC_THRESHOLD)
				.map(Map.Entry::getKey)
				.toList();
	}

	private Predicate<AgentData> canServerBeDisabled(final List<String> serversToConsider) {
		return agentData -> {
			var serverMonitoringData = (ServerMonitoringData) agentData.monitoringData();
			return serverMonitoringData.getServerJobs() <= SERVER_JOBS_NO_THRESHOLDS
					&& !serverMonitoringData.isDisabled()
					&& serversToConsider.contains(agentData.aid());
		};
	}

	private Map<String, Integer> getServersNotInDatabase(List<AgentData> agentDataFromDatabase) {
		final List<String> consideredServers = managingAgent.getGreenCloudStructure().getServerAgentsArgs().stream()
				.map(AgentArgs::getName).toList();
		final List<String> aliveServers = managingAgent.monitor().getAliveAgentsIntersection(SERVER, consideredServers);

		return managingAgent.monitor().getAgentsNotPresentInTheDatabase(agentDataFromDatabase, aliveServers).stream()
				.collect(toMap(aid -> aid, aid -> getServerMaxCapacity().applyAsInt(aid)));
	}

	private ToIntFunction<String> getServerMaxCapacity() {
		return serverAID -> {
			final List<ServerAgentArgs> allServers = managingAgent.getGreenCloudStructure().getServerAgentsArgs();

			return allServers.stream()
					.filter(server -> server.getName().equals(serverAID.split("@")[0]))
					.findFirst()
					.map(ServerAgentArgs::getIdlePower)
					.orElseThrow();
		};
	}
}
