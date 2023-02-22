package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.greencloud.commons.agent.AgentType.CNA;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.AGGREGATION_SIZE;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	private static final Logger logger = LoggerFactory.getLogger(TrafficDistributionService.class);

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent, DISTRIBUTE_TRAFFIC_EVENLY);
	}

	@Override
	public boolean evaluateAndUpdate() {
		logger.info(READ_JOB_DSTRIBUTION_LOG);
		double currentGoalQuality = computeCurrentGoalQuality();

		if (currentGoalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			logger.info(READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET);
			return true;
		}

		logger.info(JOB_DISTRIBUTION_LOG, currentGoalQuality);
		updateGoalQuality(currentGoalQuality);
		return managingAgent.monitor().isQualityInBounds(currentGoalQuality, DISTRIBUTE_TRAFFIC_EVENLY);
	}

	@Override
	public double computeCurrentGoalQuality(int time) {
		final List<Double> allQualities = new ArrayList<>();

		//CNAs
		final List<String> cnaAgents = findCNAs();
		double cnaQualities = readCNAQuality(cnaAgents);
		allQualities.add(cnaQualities);

		//Servers
		final List<List<String>> servers = findServers(cnaAgents);
		final List<Double> serversQuality = readServerQuality(servers);
		allQualities.addAll(serversQuality);

		// Worst quality
		final OptionalDouble worstQuality = allQualities.stream()
				.filter(quality -> quality != DATA_NOT_AVAILABLE_INDICATOR)
				.mapToDouble(Double::doubleValue)
				.max();

		return worstQuality.orElse(DATA_NOT_AVAILABLE_INDICATOR);
	}

	@VisibleForTesting
	protected double computeCoefficient(final List<Double> traffic) {
		int n = traffic.size();
		if (n <= 1) {
			return 0;
		}
		final OptionalDouble mean = traffic.stream().mapToDouble(Double::doubleValue).average();
		if (mean.orElseThrow() == 0.0) {
			return 0;
		}
		double sum = traffic.stream().mapToDouble(data -> Math.pow(data - mean.getAsDouble(), 2)).sum();
		double sd = Math.sqrt(sum / (n - 1));
		return sd / mean.getAsDouble();
	}

	@VisibleForTesting
	protected double computeGoalQualityForAgent(List<AgentData> data, ToDoubleFunction<AgentData> mapToCapacity) {
		var groupedData = data.stream().collect(groupingBy(AgentData::aid));
		List<Double> availableCapacities = groupedData.values().stream()
				.map(agentData -> agentData.stream().mapToDouble(mapToCapacity).average().orElseThrow()).toList();
		return computeCoefficient(availableCapacities);
	}

	private List<String> findCNAs() {
		return managingAgent.monitor().getAliveAgents(CNA);
	}

	private List<List<String>> findServers(List<String> cnaAgents) {
		List<String> servers = managingAgent.monitor().getAliveAgents(SERVER);
		return groupServersByCNA(servers, cnaAgents);
	}

	private double readCNAQuality(final List<String> cnaAgents) {
		final List<AgentData> cloudNetworkMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
				.readLatestNRowsMonitoringDataForDataTypeAndAID(CLOUD_NETWORK_MONITORING, cnaAgents, AGGREGATION_SIZE);
		if (!allAgentsHaveData(cloudNetworkMonitoringData, cnaAgents) || cloudNetworkMonitoringData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		} else if (isTrafficZero(cloudNetworkMonitoringData)) {
			return 0;
		}
		return computeGoalQualityForAgent(cloudNetworkMonitoringData, this::mapAgentDataToCNAAvailableCapacity);
	}

	private List<Double> readServerQuality(List<List<String>> servers) {
		return servers.stream().map(serversList -> {
			final List<AgentData> serverMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
					.readLatestNRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING, serversList, AGGREGATION_SIZE);

			if (!allAgentsHaveData(serverMonitoringData, serversList) || serversList.isEmpty()) {
				return (double) DATA_NOT_AVAILABLE_INDICATOR;
			} else if (isTrafficZero(serverMonitoringData)) {
				return 0.0;
			} else {
				return computeGoalQualityForAgent(serverMonitoringData, this::mapAgentDataToServerAvailableCapacity);
			}
		}).toList();
	}

	@VisibleForTesting
	protected double mapAgentDataToServerAvailableCapacity(AgentData agentData) {
		double currentMaximumCapacity = ((ServerMonitoringData) agentData.monitoringData()).getCurrentMaximumCapacity();
		return currentMaximumCapacity - (currentMaximumCapacity
				* ((ServerMonitoringData) agentData.monitoringData()).getCurrentTraffic());
	}

	@VisibleForTesting
	protected double mapAgentDataToCNAAvailableCapacity(AgentData data) {
		return ((CloudNetworkMonitoringData) data.monitoringData()).getAvailablePower();
	}

	private List<List<String>> groupServersByCNA(List<String> aliveServers, List<String> cnaAgents) {
		final List<List<String>> serversList = new ArrayList<>();
		cnaAgents.forEach(cna -> {
			final List<String> childServers = managingAgent.getGreenCloudStructure()
					.getServersForCloudNetworkAgent(cna.split("@")[0]);
			final List<String> aliveChildServers = childServers.stream().filter(childServer -> aliveServers.stream()
					.anyMatch(server -> server.split("@")[0].equals(childServer))).toList();
			serversList.add(aliveChildServers);
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
