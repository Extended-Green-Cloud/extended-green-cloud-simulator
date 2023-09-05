package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.planner.plans.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsBackUpPower;
import org.greencloud.managingsystem.service.planner.plans.domain.AgentsPowerShortages;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
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
	private Map<AgentsBackUpPower, List<AgentsPowerShortages>> greenSourcesPerServers;

	public DecrementGreenSourceErrorPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(DECREASE_GREEN_SOURCE_ERROR, managingAgent, violatedGoal);
		this.greenSourcesPerServers = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some active servers which average back up power during last timespan
	 * was above the desired threshold (i.e. green sources use considerably more back-up power than green power)
	 * 2. the aforementioned servers have at least 1 green source which weather prediction error is greater
	 * than 0.02 (i.e. the green sources have prediction error above minimal value)
	 * 3. the considered green sources had no more than 2 power shortages during last timespan (i.e. the green sources
	 * do not suffer for inconsistent weather conditions)
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final double threshold = managingAgent.monitor().getAdaptationGoal(MINIMIZE_USED_BACKUP_POWER).threshold();
		final List<AgentsBackUpPower> consideredServers = getConsideredServers(threshold);

		// verifying if servers of interest are present
		if (consideredServers.isEmpty()) {
			return false;
		}

		// verify if there are green sources satisfying given conditions per selected servers
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

		final AgentsBackUpPower selectedServer = greenSourcesPerServers.keySet().stream()
				.max(comparingDouble(AgentsBackUpPower::value))
				.orElseThrow();
		final String selectedGreenSource = greenSourcesPerServers.get(selectedServer).stream()
				.min(comparingInt(AgentsPowerShortages::value))
				.orElseThrow()
				.name();

		targetAgent = new AID(selectedGreenSource, AID.ISGUID);
		actionParameters = ImmutableAdjustGreenSourceErrorParameters.builder()
				.percentageChange(-PERCENTAGE_DIFFERENCE).build();
		return this;
	}

	@VisibleForTesting
	protected List<AgentsBackUpPower> getConsideredServers(final double threshold) {
		final List<String> aliveServers = managingAgent.monitor().getActiveServers();

		if (aliveServers.isEmpty()) {
			return emptyList();
		}

		final ToDoubleFunction<AgentData> getBackUpUsage =
				data -> ((ServerMonitoringData) data.monitoringData()).getCurrentBackUpPowerTraffic();
		final Predicate<Map.Entry<String, Double>> isBackUpWithinThreshold = entry -> entry.getValue() > threshold;

		return managingAgent.monitor().getAverageValuesForAgents(SERVER_MONITORING, aliveServers, getBackUpUsage)
				.entrySet().stream()
				.filter(isBackUpWithinThreshold)
				.map(entry -> new AgentsBackUpPower(entry.getKey(), entry.getValue()))
				.toList();
	}

	@VisibleForTesting
	protected Map<AgentsBackUpPower, List<AgentsPowerShortages>> getGreenSourcesPerServers(
			final List<AgentsBackUpPower> servers) {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_SOURCE);

		if (aliveGreenSources.isEmpty()) {
			return emptyMap();
		}

		return servers.stream()
				.map(server -> getValidGreenSourcesForServer(server, aliveGreenSources))
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@VisibleForTesting
	protected Map.Entry<AgentsBackUpPower, List<AgentsPowerShortages>> getValidGreenSourcesForServer(
			final AgentsBackUpPower server, final List<String> aliveGreenSources) {

		final List<String> greenSourcesForServer = managingAgent.getGreenCloudStructure()
				.getGreenSourcesForServerAgent(server.name().split("@")[0]);
		final List<String> consideredGreenSources = getGreenSourcesWithErrorInBounds(
				managingAgent.monitor().getAliveAgentsIntersection(aliveGreenSources, greenSourcesForServer));

		return new AbstractMap.SimpleEntry<>(server,
				getGreenSourcesWithCorrectPowerShortageCount(consideredGreenSources));
	}

	@VisibleForTesting
	protected List<AgentsPowerShortages> getGreenSourcesWithCorrectPowerShortageCount(
			final List<String> consideredGreenSources) {
		final ToIntFunction<AgentData> getShortageCount = data ->
				((WeatherShortages) data.monitoringData()).weatherShortagesNumber();
		final Predicate<Map.Entry<String, Integer>> isPowerShortageCountCorrect = entry ->
				entry.getValue() < POWER_SHORTAGE_THRESHOLD;

		final List<AgentData> weatherShortagesForAgents = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, consideredGreenSources,
						MONITOR_SYSTEM_DATA_TIME_PERIOD);
		final Stream<AgentsPowerShortages> agentsWithoutPowerShortages = managingAgent.monitor()
				.getAgentsNotPresentInTheDatabase(weatherShortagesForAgents, consideredGreenSources).stream()
				.map(agent -> new AgentsPowerShortages(agent, 0));
		final Stream<AgentsPowerShortages> agentWithCorrectPowerShortageCount = weatherShortagesForAgents.stream()
				.collect(groupingBy(AgentData::aid, TreeMap::new, summingInt(getShortageCount)))
				.entrySet().stream()
				.filter(isPowerShortageCountCorrect)
				.map(entry -> new AgentsPowerShortages(entry.getKey(), entry.getValue()));

		return Stream.concat(agentWithCorrectPowerShortageCount, agentsWithoutPowerShortages).toList();
	}

	@VisibleForTesting
	protected List<String> getGreenSourcesWithErrorInBounds(final List<String> consideredGreenSources) {
		final Predicate<AgentData> isErrorCorrect = data ->
				consideredGreenSources.contains(data.aid()) &&
						((GreenSourceMonitoringData) data.monitoringData()).getWeatherPredictionError()
								> MINIMUM_PREDICTION_ERROR;

		return managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING)).stream()
				.collect(filtering(isErrorCorrect, mapping(AgentData::aid, toList())));
	}

	@VisibleForTesting
	protected Map<AgentsBackUpPower, List<AgentsPowerShortages>> getGreenSourcesPerServers() {
		return greenSourcesPerServers;
	}

	@VisibleForTesting
	protected void setGreenSourcesPerServers(
			Map<AgentsBackUpPower, List<AgentsPowerShortages>> greenSourcesPerServers) {
		this.greenSourcesPerServers = greenSourcesPerServers;
	}
}
