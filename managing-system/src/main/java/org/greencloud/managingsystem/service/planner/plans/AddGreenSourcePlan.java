package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.max;
import static java.util.Comparator.comparingDouble;
import static java.util.Objects.isNull;
import static org.greencloud.managingsystem.service.planner.logs.ManagingAgentPlannerLog.NO_LOCATION_LOG;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.commons.agentfactory.AgentFactory;
import com.greencloud.commons.agentfactory.AgentFactoryImpl;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddGreenSourceActionParameters;

import jade.core.Location;

/**
 * Class containing adaptation plan which realizes the action of adding new green source to the system
 */
public class AddGreenSourcePlan extends SystemPlan {

	private static final Logger logger = LoggerFactory.getLogger(AddGreenSourcePlan.class);

	private static final Double RELATIVE_BACKUP_POWER_VALUE_THRESHOLD = 0.2;
	private static final Predicate<Double> IS_OVER_THRESHOLD = value -> value >= RELATIVE_BACKUP_POWER_VALUE_THRESHOLD;

	private final AgentFactory agentFactory;

	private Map<String, List<Double>> serversData;

	public AddGreenSourcePlan(ManagingAgent managingAgent) {
		super(ADD_GREEN_SOURCE, managingAgent);
		agentFactory = new AgentFactoryImpl();
		serversData = emptyMap();
	}

	/**
	 * Tests if plan is executable, that is if any server in the system was using back up power in the last
	 * MONITOR_SYSTEM_DATA_PERIOD over specified relative back up power threshold
	 * ({@link AddGreenSourcePlan#IS_OVER_THRESHOLD}).
	 *
	 * @return result of the test
	 */
	@Override
	public boolean isPlanExecutable() {
		serversData = getServerData().stream()
				.collect(Collectors.groupingBy(AgentData::aid))
				.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, this::getBackUpPowerForServer));
		return serversData.entrySet().stream()
				.anyMatch(entry -> entry.getValue().stream().anyMatch(IS_OVER_THRESHOLD));
	}

	/**
	 * Finds a server with a maximum backup power and adds there a new green source
	 *
	 * @return constructed adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		String targetServer = max(serversData.entrySet(), comparingDouble(entry -> max(entry.getValue()))).getKey();

		if (isNull(targetServer)) {
			return null;
		}

		ServerAgentArgs targetServerArgs = managingAgent.getGreenCloudStructure()
				.getServerAgentsArgs().stream()
				.filter(serverAgentArgs -> targetServer.contains(serverAgentArgs.getName()))
				.findFirst()
				.orElse(null);

		if (isNull(targetServerArgs)) {
			return null;
		}

		String targetCloudNetworkAgent = targetServerArgs.getOwnerCloudNetwork();
		MonitoringAgentArgs extraMonitoringAgentArguments = agentFactory.createMonitoringAgent();
		GreenEnergyAgentArgs extraGreenEnergyArguments = agentFactory.createDefaultGreenEnergyAgent(
				extraMonitoringAgentArguments.getName(), targetServerArgs.getName());
		Location targetLocation = findTargetLocation(targetCloudNetworkAgent);

		if (isNull(targetLocation)) {
			logger.warn(NO_LOCATION_LOG);
			return null;
		}

		actionParameters = new AddGreenSourceActionParameters(extraGreenEnergyArguments, extraMonitoringAgentArguments,
				targetLocation);

		return this;
	}

	private List<Double> getBackUpPowerForServer(Map.Entry<String, List<AgentData>> entry) {
		return entry.getValue().stream()
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.map(this::getRelativeBackUpPower)
				.collect(Collectors.toList());
	}

	private double getRelativeBackUpPower(ServerMonitoringData data) {
		return data.getCurrentBackUpPowerUsage() / (data.getCurrentTraffic() + data.getCurrentBackUpPowerUsage());
	}
}
