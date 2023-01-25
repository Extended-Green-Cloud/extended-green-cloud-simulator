package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.greencloud.commons.agent.AgentType.CNA;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_UNSATISFIED_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	public static final GoalEnum GOAL = DISTRIBUTE_TRAFFIC_EVENLY;
	public static final int AGGREGATION_SIZE = 10;
	private static final Logger logger = LoggerFactory.getLogger(TrafficDistributionService.class);

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		List<Double> allQualities = new ArrayList<>();

		//CNAs
		List<String> CNAs = findCNAs();
		double CNAQuality = readCNAQuality(CNAs);
		allQualities.add(CNAQuality);

		//Servers
		List<List<String>> servers = findServers(CNAs);
		List<Double> serversQuality = readServerQuality(servers);
		allQualities.addAll(serversQuality);

		// Worst quality
		OptionalDouble worstQuality = allQualities.stream().filter(q -> q != DATA_NOT_AVAILABLE_INDICATOR)
				.mapToDouble(Double::doubleValue).max();

		if (worstQuality.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		}

		return worstQuality.getAsDouble();
	}

	@Override
	public boolean evaluateAndUpdate() {
		logger.info(READ_JOB_DSTRIBUTION_LOG);
		double currentGoalQuality = readCurrentGoalQuality(MONITOR_SYSTEM_DATA_TIME_PERIOD);

		if (currentGoalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			logger.info(READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET);
			return true;
		}

		logger.info(JOB_DISTRIBUTION_LOG, currentGoalQuality);
		updateGoalQuality(GOAL, currentGoalQuality);
		boolean result = managingAgent.monitor().isQualityInBounds(currentGoalQuality, DISTRIBUTE_TRAFFIC_EVENLY);

		if (!result) {
			logger.info(JOB_DISTRIBUTION_UNSATISFIED_LOG, currentGoalQuality);
		}

		return result;
	}

	@VisibleForTesting
	protected double computeCoefficient(List<Double> traffic) {
		int n = traffic.size();
		if (n <= 1) {
			return 0;
		}
		OptionalDouble mean = traffic.stream().mapToDouble(Double::doubleValue).average();
		if (mean.orElseThrow() == 0.0) {
			return 0;
		}
		double sum = traffic.stream().mapToDouble(data -> Math.pow(data - mean.getAsDouble(), 2)).sum();
		double sd = Math.sqrt(sum / (n - 1));
		return sd / mean.getAsDouble();
	}

	@VisibleForTesting
	protected double computeGoalQualityForCNA(List<AgentData> data) {
		var groupedData = data.stream()
				.collect(groupingBy(AgentData::aid));
		List<Double> availableCapacities = groupedData.values().stream()
				.map(agentData -> agentData.stream()
						.mapToDouble(this::mapAgentDataToCNAAvailableCapacity)
						.average().orElseThrow())
				.toList();
		return computeCoefficient(availableCapacities);
	}

	@VisibleForTesting
	protected double computeGoalQualityForServer(List<AgentData> data) {
		var groupedData = data.stream()
				.collect(groupingBy(AgentData::aid));
		List<Double> availableCapacities = groupedData.values().stream()
				.map(agentData -> agentData.stream()
					.mapToDouble(this::mapAgentDataToServerAvailableCapacity)
					.average().orElseThrow())
				.toList();
		return computeCoefficient(availableCapacities);
	}

	private List<String> findCNAs() {
		return managingAgent.monitor().getAliveAgents(CNA);
	}

	private List<List<String>> findServers(List<String> CNAs) {
		List<String> servers = managingAgent.monitor().getAliveAgents(SERVER);
		return groupServersByCNA(servers, CNAs);
	}

	private double readCNAQuality(List<String> CNAs) {
		List<AgentData> cloudNetworkMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
				.readLatestNRowsMonitoringDataForDataTypeAndAID(CLOUD_NETWORK_MONITORING, CNAs, AGGREGATION_SIZE);
		if (!allAgentsHaveData(cloudNetworkMonitoringData, CNAs) || cloudNetworkMonitoringData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		} else if (isTrafficZero(cloudNetworkMonitoringData)) {
			return 0;
		}
		return computeGoalQualityForCNA(cloudNetworkMonitoringData);
	}

	private List<Double> readServerQuality(List<List<String>> servers) {
		List<Double> serversQuality = new ArrayList<>();
		servers.forEach(serversList -> {
			List<AgentData> serverMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
					.readLatestNRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING, serversList, AGGREGATION_SIZE);
			if (!allAgentsHaveData(serverMonitoringData, serversList) || serversList.isEmpty()) {
				serversQuality.add((double) DATA_NOT_AVAILABLE_INDICATOR);
			} else if (isTrafficZero(serverMonitoringData)) {
				serversQuality.add(0.0);
			} else {
				serversQuality.add(computeGoalQualityForServer(serverMonitoringData));
			}
		});
		return serversQuality;
	}

	private double mapAgentDataToServerAvailableCapacity(AgentData agentData) {
		double currentMaximumCapacity = ((ServerMonitoringData) agentData.monitoringData()).getCurrentMaximumCapacity();
		return currentMaximumCapacity - (currentMaximumCapacity
				* ((ServerMonitoringData) agentData.monitoringData()).getCurrentTraffic());
	}

	private double mapAgentDataToCNAAvailableCapacity(AgentData data) {
		return ((CloudNetworkMonitoringData) data.monitoringData()).getAvailablePower();
	}

	private List<List<String>> groupServersByCNA(List<String> aliveServers, List<String> CNAs) {
		List<List<String>> serversList = new ArrayList<>();
		CNAs.forEach(CNA -> {
			List<String> servers = managingAgent.getGreenCloudStructure()
					.getServersForCloudNetworkAgent(CNA.split("@")[0]);
			List<String> childServers = new ArrayList<>();
			aliveServers.forEach(server -> {
				if (servers.contains(server.split("@")[0])) {
					childServers.add(server);
				}
			});
			serversList.add(childServers);
		});
		return serversList;
	}

	private boolean allAgentsHaveData(List<AgentData> data, List<String> agents) {
		return new HashSet<>(data.stream().map(AgentData::aid).toList()).containsAll(agents);
	}

	private boolean isTrafficZero(List<AgentData> data) {
		final Predicate<AgentData> isTrafficZero = agentData ->
				((NetworkComponentMonitoringData) agentData.monitoringData()).getCurrentTraffic() == 0;
		return data.stream()
				.collect(groupingBy(AgentData::aid)).values().stream()
				.map(dataSet -> dataSet.stream().max(comparing(AgentData::timestamp)).orElseThrow())
				.allMatch(isTrafficZero);
	}
}
