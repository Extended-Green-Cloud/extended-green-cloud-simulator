package org.greencloud.managingsystem.service.planner.plans;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.managingsystem.planner.ImmutableDisableServerActionParameters;

import jade.core.AID;

import org.greencloud.managingsystem.agent.ManagingAgent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DISABLE_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.stream.Collectors.toMap;

/**
 * Class containing adaptation plan which realizes the action of disabling the idle server
 */
public class DisableServerPlan extends AbstractPlan {

	/**
	 * Default abstract constructor
	 *
	 * @param managingAgent managing agent executing the action
	 */
	public DisableServerPlan(ManagingAgent managingAgent) {
		super(DISABLE_SERVER, managingAgent);
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. There are some idle servers in the system (servers that current traffic is equal to zero)
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		List<AgentData> serverMonitoring = managingAgent.getAgentNode().getDatabaseClient().
				readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

		AtomicBoolean idleExists = new AtomicBoolean(false);

		serverMonitoring.stream().forEach(data -> {
			var serverMonitoringData = (ServerMonitoringData) data.monitoringData();
			if (serverMonitoringData.getCurrentTraffic() == 0 && !serverMonitoringData.isDisabled()) {
				idleExists.set(true);
			}
		});
		return idleExists.get();
	}

	/**
	 * Method chooses the idle server to disable. The server is chosen based on the following criteria:
	 * 1. The server with the highest maximum capacity
	 *
	 * @return Constructed plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		List<AgentData> serverMonitoring = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(List.of(SERVER_MONITORING));

		targetAgent = selectTargetAgent(serverMonitoring);

		actionParameters = ImmutableDisableServerActionParameters.builder()
				.build();

		return this;
	}

	protected AID selectTargetAgent(List<AgentData> data) {

		Map<String, Integer> consideredServers = getAgentsNotInDatabase(data);

		List<AgentData> idleServers = data.stream()
				.filter(monitoring -> ((ServerMonitoringData) monitoring.monitoringData()).getCurrentTraffic() == 0)
				.toList();

		consideredServers.putAll(idleServers.stream().collect(toMap(AgentData::aid,
				agentData -> ((ServerMonitoringData) agentData.monitoringData()).getCurrentMaximumCapacity())));

		String chosenServer = Collections.max(consideredServers.entrySet(),
				Map.Entry.comparingByValue()).getKey();
		return new AID(chosenServer, AID.ISGUID);
	}

	private Map<String, Integer> getAgentsNotInDatabase(List<AgentData> data) {
		Map<String, Integer> allConsideredAgentsToMaxCapacity = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs()
				.stream().collect(toMap(AgentArgs::getName, arg -> Integer.parseInt(arg.getMaximumCapacity())));

		allConsideredAgentsToMaxCapacity = updateKeysToAID(allConsideredAgentsToMaxCapacity);

		List<String> agentsInDataBase = data.stream().map(AgentData::aid).toList();
		return allConsideredAgentsToMaxCapacity.entrySet().stream()
				.filter(entry -> !agentsInDataBase.contains(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<String, Integer> updateKeysToAID(Map<String, Integer> map) {
		List<String> aliveServers = managingAgent.monitor().getAliveAgents(AgentType.SERVER);

		aliveServers.stream().forEach(name -> {
			var shortName = name.split("@")[0];
			if (map.containsKey(shortName)) {
				int tmp = map.get(shortName);
				map.remove(shortName);
				map.put(name, tmp);
			}
		});
		return map;
	}
}
