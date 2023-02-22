package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Collections.max;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.ImmutableDisableServerActionParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of disabling the idle server
 */
public class DisableServerPlan extends AbstractPlan {

	private final Predicate<AgentData> canServerBeDisabled = agentData -> {
		var serverMonitoringData = (ServerMonitoringData) agentData.monitoringData();
		return serverMonitoringData.getCurrentTraffic() == 0 && !serverMonitoringData.isDisabled();
	};
	private List<AgentData> idleServers;

	public DisableServerPlan(ManagingAgent managingAgent) {
		super(DISABLE_SERVER, managingAgent);
		idleServers = new ArrayList<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some idle servers in the system (servers that current traffic is equal to zero)
	 * 2. those servers haven't been yet disabled
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> serverMonitoring = managingAgent.getAgentNode().getDatabaseClient().
				readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

		idleServers = serverMonitoring.stream()
				.filter(canServerBeDisabled)
				.toList();

		return !idleServers.isEmpty();
	}

	/**
	 * Method chooses the idle server to disable. The server is chosen based on the following criteria:
	 * 1. The server with the highest maximum capacity
	 *
	 * @return Constructed plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final Map<String, Integer> consideredServers = idleServers.stream()
				.collect(toMap(AgentData::aid, data -> getServerCurrentMaxCapacity().applyAsInt(data)));

		// consider also servers which has not registered any data in database (i.e. did not accept any jobs)
		consideredServers.putAll(getAgentsNotInDatabase(idleServers));

		final String chosenServer = max(consideredServers.entrySet(), comparingByValue()).getKey();
		targetAgent = new AID(chosenServer, AID.ISGUID);

		actionParameters = ImmutableDisableServerActionParameters.builder().build();

		return this;
	}

	private Map<String, Integer> getAgentsNotInDatabase(List<AgentData> agentDataFromDatabase) {
		final List<String> consideredServers = managingAgent.getGreenCloudStructure().getServerAgentsArgs().stream()
				.map(AgentArgs::getName).toList();
		final List<String> aliveServers = managingAgent.monitor().getAliveAgentsIntersection(SERVER, consideredServers);

		return managingAgent.monitor().getAgentsNotPresentInTheDatabase(agentDataFromDatabase, aliveServers).stream()
				.collect(toMap(aid -> aid, aid -> getServerMaxCapacity().applyAsInt(aid)));
	}

	private ToIntFunction<AgentData> getServerCurrentMaxCapacity() {
		return serverData -> ((ServerMonitoringData) serverData.monitoringData()).getCurrentMaximumCapacity();
	}

	private ToIntFunction<String> getServerMaxCapacity() {
		return serverAID -> {
			final List<ServerAgentArgs> allServers = managingAgent.getGreenCloudStructure().getServerAgentsArgs();

			return allServers.stream()
					.filter(server -> server.getName().equals(serverAID.split("@")[0]))
					.findFirst()
					.map(ServerAgentArgs::getMaximumCapacity)
					.map(Integer::parseInt)
					.orElseThrow();
		};
	}
}
