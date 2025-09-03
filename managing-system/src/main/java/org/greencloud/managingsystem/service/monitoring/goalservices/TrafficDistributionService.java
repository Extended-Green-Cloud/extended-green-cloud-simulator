package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.types.DataType.SERVER_MONITORING;
import static com.database.knowledge.types.GoalType.DISTRIBUTE_TRAFFIC_EVENLY;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.google.common.annotations.VisibleForTesting;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	private static final LogBuilder log = constructLogBuilder(TrafficDistributionService.class);

	public TrafficDistributionService(ManagingAgent managingAgent) {
		super(managingAgent, DISTRIBUTE_TRAFFIC_EVENLY);
	}

	@Override
	public boolean evaluateAndUpdate() {
		log.messageInfo("Reading job distribution coefficients from database...");
		double currentGoalQuality = computeCurrentGoalQuality();

		if (currentGoalQuality == DATA_NOT_AVAILABLE_INDICATOR) {
			log.messageInfo("Job distribution data is not available yet");
			return true;
		}

		log.messageInfo("Current job distribution: {}", currentGoalQuality);
		updateGoalQuality(currentGoalQuality);
		return managingAgent.monitor().isQualityInBounds(currentGoalQuality, DISTRIBUTE_TRAFFIC_EVENLY);
	}

	@Override
	public double computeCurrentGoalQuality(final int time) {
		final List<String> rmaAgents = managingAgent.monitor().getAliveAgents(REGIONAL_MANAGER);

		// Servers
		final List<String> servers = managingAgent.monitor().getActiveServers();
		final Map<String, List<String>> serversPerRMA = groupServersByRMA(servers, rmaAgents);
		final List<Pair<Double, Double>> serverValues = readServerQuality(serversPerRMA);
		final List<Double> allQualities = new java.util.ArrayList<>(serverValues.stream().map(Pair::getRight).toList());

		//RMAs
		final List<Double> regionPowerConsumption = serverValues.stream().map(Pair::getLeft).toList();
		final double regionsQuality = regionPowerConsumption.stream().allMatch(val -> val <= 0) ? 0 :
				computeCoefficient(regionPowerConsumption);
		allQualities.add(regionsQuality);

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

	private List<Pair<Double, Double>> readServerQuality(final Map<String, List<String>> serversPerRMA) {
		final int goalWindowSize = managingAgent.getProperties().getConfig().getQualityGoalsWindowSize();

		return serversPerRMA.entrySet().stream().map(entry -> {
			final List<String> serversList = entry.getValue();
			final List<AgentData> serverMonitoringData = managingAgent.getAgentNode().getDatabaseClient()
					.readLatestNRowsMonitoringDataForDataTypeAndAID(SERVER_MONITORING, serversList, goalWindowSize);

			if (serverMonitoringData.isEmpty()) {
				log.messageInfo("There is no monitoring data available for servers under {}", entry.getKey());

				final double notAvailable = DATA_NOT_AVAILABLE_INDICATOR;
				return Pair.of(notAvailable, notAvailable);
			}

			if (isTrafficZero(serverMonitoringData)) {
				return Pair.of(0D, 0D);
			}

			final List<Double> powerConsumption = computePowerConsumptionForAgents(serverMonitoringData, serversList);
			final double totalPowerConsumption = powerConsumption.stream().mapToDouble(Double::doubleValue).sum();
			final double goalQuality = computeCoefficient(powerConsumption);
			return Pair.of(totalPowerConsumption, goalQuality);

		}).toList();
	}

	protected List<Double> computePowerConsumptionForAgents(final List<AgentData> data,
			final List<String> consideredAgents) {
		final Map<String, List<AgentData>> agentsData = data.stream().collect(groupingBy(AgentData::aid));

		final Stream<Double> powerConsumptions = agentsData.values().stream()
				.map(agentData -> agentData.stream()
						.mapToDouble(this::mapToPowerConsumption)
						.filter(Objects::nonNull)
						.average().orElseThrow());
		final Stream<Double> consumptionForAgentsWithNoRecords = managingAgent.monitor()
				.getAgentsNotPresentInTheDatabase(data, consideredAgents).stream()
				.map(_ -> 0.0D);

		return concat(powerConsumptions, consumptionForAgentsWithNoRecords).toList();
	}

	@VisibleForTesting
	protected Double mapToPowerConsumption(AgentData data) {
		return ((ServerMonitoringData) data.monitoringData()).getCurrentPowerConsumption();
	}

	private Map<String, List<String>> groupServersByRMA(final List<String> aliveServers, final List<String> rmaAgents) {
		return rmaAgents.stream().collect(toMap(rma -> rma, rma -> {
			final List<String> childServers = managingAgent.getProperties().getGreenCloudStructure()
					.getServersForRegionalManagerAgent(rma.split("@")[0]);

			return childServers.stream().filter(childServer -> aliveServers.stream()
					.anyMatch(server -> server.split("@")[0].equals(childServer))).toList();
		}));
	}

	private boolean isTrafficZero(final List<AgentData> data) {
		final Predicate<AgentData> isTrafficZero = agentData ->
				((ServerMonitoringData) agentData.monitoringData()).getCurrentPowerConsumption() == 0;
		return data.stream()
				.collect(groupingBy(AgentData::aid)).values().stream()
				.map(dataSet -> dataSet.stream().max(comparing(AgentData::timestamp)).orElseThrow())
				.allMatch(isTrafficZero);
	}
}
