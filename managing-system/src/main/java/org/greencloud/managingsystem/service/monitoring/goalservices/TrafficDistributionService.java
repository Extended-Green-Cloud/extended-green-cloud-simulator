package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.greencloud.commons.agent.AgentType.CNA;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.AGGREGATION_SIZE;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.JOB_DISTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG;
import static org.greencloud.managingsystem.service.monitoring.logs.ManagingAgentMonitoringLog.READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
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
	public double computeCurrentGoalQuality(final int time) {
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
	protected double computeGoalQualityForAgent(final List<AgentData> data, final List<String> consideredAgents) {
		var groupedData = data.stream().collect(groupingBy(AgentData::aid));

		final Stream<Double> availableCapacities = groupedData.values().stream()
				.map(agentData -> agentData.stream()
						.mapToDouble(this::mapToAvailableCapacity)
						.filter(Objects::nonNull)
						.average().orElseThrow());
		final Stream<Double> capacityForAgentsWithNoRecords = managingAgent.monitor()
				.getAgentsNotPresentInTheDatabase(data, consideredAgents).stream()
				.map(agent -> 0.0D);

		return computeCoefficient(concat(availableCapacities, capacityForAgentsWithNoRecords).toList());
	}

	private List<String> findCNAs() {
		return managingAgent.monitor().getAliveAgents(CNA);
	}

	private List<List<String>> findServers(List<String> cnaAgents) {
		final List<String> servers = managingAgent.monitor().getActiveServers();
		return groupServersByCNA(servers, cnaAgents);
	}

	private double readCNAQuality(final List<String> cnaAgents) {
		final List<AgentData> cloudNetworkMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
				.readLatestNRowsMonitoringDataForDataTypeAndAID(CLOUD_NETWORK_MONITORING, cnaAgents, AGGREGATION_SIZE);

		if (cloudNetworkMonitoringData.isEmpty()) {
			return DATA_NOT_AVAILABLE_INDICATOR;
		} else if (isTrafficZero(cloudNetworkMonitoringData)) {
			return 0;
		}
		return computeGoalQualityForAgent(cloudNetworkMonitoringData, cnaAgents);
	}

	private List<Double> readServerQuality(List<List<String>> servers) {
		return servers.stream().map(serversList -> {
			final List<AgentData> serverMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
					.readLatestNRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING, serversList, AGGREGATION_SIZE);

			if (serverMonitoringData.isEmpty()) {
				return (double) DATA_NOT_AVAILABLE_INDICATOR;
			} else if (isTrafficZero(serverMonitoringData)) {
				return 0.0;
			} else {
				return computeGoalQualityForAgent(serverMonitoringData, serversList);
			}
		}).toList();
	}

	@VisibleForTesting
	protected Double mapToAvailableCapacity(AgentData data) {
		return ((NetworkComponentMonitoringData) data.monitoringData()).getAvailablePower();
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

	private boolean isTrafficZero(List<AgentData> data) {
		final Predicate<AgentData> isTrafficZero = agentData ->
				((NetworkComponentMonitoringData) agentData.monitoringData()).getCurrentTraffic() == 0;
		return data.stream()
				.collect(groupingBy(AgentData::aid)).values().stream()
				.map(dataSet -> dataSet.stream().max(comparing(AgentData::timestamp)).orElseThrow())
				.allMatch(isTrafficZero);
	}
}
