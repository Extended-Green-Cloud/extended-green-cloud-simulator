package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ENABLE_SERVER;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.greencloud.commons.managingsystem.planner.ImmutableEnableServerActionParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan that enables a given server back
 */
public class EnableServerPlan extends AbstractPlan {

	protected static final double TRAFFIC_LOAD_THRESHOLD = 0.2;

	private Map<String, Integer> consideredServers;

	public EnableServerPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(ENABLE_SERVER, managingAgent, violatedGoal);
		consideredServers = emptyMap();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some disabled servers in the system
	 * 2. all servers have traffic load over specified constant TRAFFIC_LOAD_THRESHOLD value
	 * (in order to make sure that new servers are not unnecessarily enabled) - it is similar to add server case
	 * but the required threshold is lower
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> disabledServers = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(singletonList(SERVER_MONITORING)).stream()
				.filter(data -> ((ServerMonitoringData) data.monitoringData()).isDisabled())
				.toList();

		// verifying if there are some disabled servers
		if (disabledServers.isEmpty()) {
			return false;
		}

		// verifying the saturation of enabled servers
		final boolean areServersFull = managingAgent.monitor().getActiveServersData().stream()
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.mapToDouble(ServerMonitoringData::getCurrentTraffic)
				.allMatch(traffic -> traffic > TRAFFIC_LOAD_THRESHOLD);

		if (!areServersFull) {
			return false;
		}

		// getting servers data
		consideredServers = disabledServers.stream()
				.collect(toMap(AgentData::aid,
						data -> ((ServerMonitoringData) data.monitoringData()).getIdlePowerConsumption()));

		return true;
	}

	/**
	 * The plan select the server that has the lowest idle power consumption
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		final String selectedServer = consideredServers.entrySet().stream().min(comparingInt(Map.Entry::getValue))
				.orElse(consideredServers.entrySet().stream().findFirst().orElseThrow())
				.getKey();

		targetAgent = new AID(selectedServer, AID.ISGUID);
		actionParameters = ImmutableEnableServerActionParameters.builder().build();

		return this;
	}
}
