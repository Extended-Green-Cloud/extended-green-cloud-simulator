package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static com.greencloud.commons.agent.AgentType.SERVER;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.planner.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.google.common.annotations.VisibleForTesting;
import com.greencloud.commons.managingsystem.planner.ImmutableAdjustGreenSourceErrorParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of decrementing
 * the weather prediction error for given Green Source
 */
public class DecrementGreenSourceErrorPlan extends AbstractPlan {

	protected static final double PERCENTAGE_DIFFERENCE = 0.02;
	private static final double MINIMUM_PREDICTION_ERROR = 0.02;
	private Map<Map.Entry<String, Double>, Map<String, Integer>> greenSourcesPerServers;

	public DecrementGreenSourceErrorPlan(ManagingAgent managingAgent) {
		super(DECREASE_GREEN_SOURCE_ERROR, managingAgent);
		this.greenSourcesPerServers = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some servers which average back up power during last 5 seconds of simulation
	 * was above the desired threshold
	 * 2. the aforementioned servers have at least 1 green source which weather prediction error is greater
	 * than 0.02
	 * 3. the considered green sources had no more than 2 power shortages during last 5s
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final double threshold = managingAgent.monitor().getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER).threshold();
		final Map<String, Double> consideredServers = getConsideredServers(threshold);

		// verifying if servers of interest are present
		if (consideredServers.isEmpty()) {
			return false;
		}

		greenSourcesPerServers = getGreenSourcesPerServers(consideredServers);

		return !greenSourcesPerServers.isEmpty();
	}

	/**
	 * Method constructs plan which computes new weather prediction error for given Green Source.
	 * The method selects the server with the highest back up power usage and the corresponding Green Source
	 * with the lowest number of power shortages
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		if (greenSourcesPerServers.isEmpty()) {
			return null;
		}

		final Map.Entry<String, Double> selectedServer = greenSourcesPerServers.keySet().stream()
				.max(comparingDouble(Map.Entry::getValue))
				.orElseThrow();
		final String selectedGreenSource = greenSourcesPerServers.get(selectedServer).entrySet().stream()
				.min(comparingInt(Map.Entry::getValue))
				.orElseThrow()
				.getKey();

		targetAgent = new AID(selectedGreenSource, AID.ISGUID);
		actionParameters = ImmutableAdjustGreenSourceErrorParameters.builder()
				.percentageChange(-PERCENTAGE_DIFFERENCE).build();
		return this;
	}

	@VisibleForTesting
	protected Map<String, Double> getConsideredServers(final double threshold) {
		final List<String> aliveServers = managingAgent.monitor().getAliveAgents(SERVER);

		if (aliveServers.isEmpty()) {
			return emptyMap();
		}

		final ToDoubleFunction<AgentData> getBackUpUsage =
				data -> ((ServerMonitoringData) data.monitoringData()).getCurrentBackUpPowerUsage();
		final Predicate<Map.Entry<String, Double>> isWithinThreshold = entry -> entry.getValue() > threshold;

		return managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(SERVER_MONITORING, aliveServers, MONITOR_SYSTEM_DATA_TIME_PERIOD)
				.stream().collect(groupingBy(AgentData::aid, TreeMap::new, averagingDouble(getBackUpUsage)))
				.entrySet().stream()
				.collect(filtering(isWithinThreshold, toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	@VisibleForTesting
	protected Map<Map.Entry<String, Double>, Map<String, Integer>> getGreenSourcesPerServers(
			final Map<String, Double> servers) {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_SOURCE);

		if (aliveGreenSources.isEmpty()) {
			return emptyMap();
		}

		return servers.entrySet().stream()
				.map(server -> getValidGreenSourcesForServer(server, aliveGreenSources))
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<Map.Entry<String, Double>, Map<String, Integer>> getValidGreenSourcesForServer(
			final Map.Entry<String, Double> server, final List<String> aliveGreenSources) {

		final List<String> greenSourcesForServer = managingAgent.getGreenCloudStructure()
				.getGreenSourcesForServerAgent(server.getKey().split("@")[0]);
		final List<String> consideredGreenSources = getGreenSourcesWithCorrectError(
				getAliveAgentsIntersection(aliveGreenSources, greenSourcesForServer));

		final ToIntFunction<AgentData> getShortageCount = data ->
				((WeatherShortages) data.monitoringData()).weatherShortagesNumber();
		final Predicate<Map.Entry<String, Integer>> isPowerShortageCountCorrect = entry ->
				entry.getValue() < POWER_SHORTAGE_THRESHOLD;

		final Map<String, Integer> validGreenSources = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, consideredGreenSources,
						MONITOR_SYSTEM_DATA_TIME_PERIOD).stream()
				.collect(groupingBy(AgentData::aid, TreeMap::new, summingInt(getShortageCount)))
				.entrySet().stream()
				.collect(filtering(isPowerShortageCountCorrect, toMap(Map.Entry::getKey, Map.Entry::getValue)));

		return new AbstractMap.SimpleEntry<>(server, validGreenSources);
	}

	@VisibleForTesting
	protected List<String> getGreenSourcesWithCorrectError(final List<String> consideredGreenSources) {
		final Predicate<AgentData> isErrorCorrect = data ->
				consideredGreenSources.contains(data.aid()) &&
						((GreenSourceMonitoringData) data.monitoringData()).getWeatherPredictionError()
								> MINIMUM_PREDICTION_ERROR;

		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING)).stream()
				.collect(filtering(isErrorCorrect, mapping(AgentData::aid, toList())));
	}

	private List<String> getAliveAgentsIntersection(List<String> allALiveAgents, List<String> allAgentsOfType) {
		final Predicate<String> isAgentNameValid = agentName -> allAgentsOfType.contains(agentName.split("@")[0]);

		return allALiveAgents.stream().filter(isAgentNameValid).toList();
	}

}
