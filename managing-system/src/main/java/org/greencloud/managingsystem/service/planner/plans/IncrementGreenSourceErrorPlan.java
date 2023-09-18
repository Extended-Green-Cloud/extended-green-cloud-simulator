package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.planner.plans.domain.AdaptationPlanVariables.POWER_SHORTAGE_THRESHOLD;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import org.greencloud.commons.args.adaptation.singleagent.ImmutableAdjustGreenSourceErrorParameters;
import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.annotations.VisibleForTesting;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of incrementing
 * the weather prediction error for given Green Source
 */
public class IncrementGreenSourceErrorPlan extends AbstractPlan {

	protected static final double PERCENTAGE_DIFFERENCE = 0.02;
	private static final int MAXIMUM_PREDICTION_ERROR = 1;
	private Map<String, Integer> greenSourcesPowerShortages;

	public IncrementGreenSourceErrorPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(INCREASE_GREEN_SOURCE_ERROR, managingAgent, violatedGoal);
		this.greenSourcesPowerShortages = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some GS alive in the system
	 * 2. there are some alive GS for which weather prediction error is less than 100%
	 * 3. there are some alive GS which had at least 2 power shortages per 5s
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> greenSourceData =
				managingAgent.getAgentNode().getDatabaseClient()
						.readLastMonitoringDataForDataTypes(singletonList(GREEN_SOURCE_MONITORING));
		final Map<String, Double> greenSourceErrorMap =
				getGreenSourcesWithErrors(greenSourceData, managingAgent.monitor().getAliveAgents(GREEN_ENERGY));

		if (greenSourceErrorMap.isEmpty()) {
			return false;
		}

		greenSourcesPowerShortages = getGreenSourcesWithPowerShortages(greenSourceErrorMap.keySet());
		return !greenSourcesPowerShortages.isEmpty();
	}

	/**
	 * Method constructs plan which computes new weather prediction error for given Green Source
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		if (greenSourcesPowerShortages.isEmpty()) {
			return null;
		}
		final String selectedAgent = greenSourcesPowerShortages.entrySet().stream()
				.max(Comparator.comparingInt(Map.Entry::getValue))
				.orElseThrow()
				.getKey();
		targetAgent = new AID(selectedAgent, AID.ISGUID);
		actionParameters = ImmutableAdjustGreenSourceErrorParameters.builder()
				.percentageChange(PERCENTAGE_DIFFERENCE).build();
		return this;
	}

	public void setGreenSourcesPowerShortages(Map<String, Integer> greenSourcesPowerShortages) {
		this.greenSourcesPowerShortages = greenSourcesPowerShortages;
	}

	@VisibleForTesting
	protected Map<String, Double> getGreenSourcesWithErrors(List<AgentData> greenSourceData, List<String> aliveAgents) {
		final Predicate<String> isAgentAlive = aliveAgents::contains;
		final Predicate<AgentData> isErrorWithinBounds = data -> {
			var error = ((GreenSourceMonitoringData) data.monitoringData()).getWeatherPredictionError();
			return error < MAXIMUM_PREDICTION_ERROR - PERCENTAGE_DIFFERENCE;
		};
		final ToDoubleFunction<AgentData> mapToError = data ->
				((GreenSourceMonitoringData) data.monitoringData()).getWeatherPredictionError();

		return greenSourceData.stream()
				.filter(agentData -> isAgentAlive.test(agentData.aid()) && isErrorWithinBounds.test(agentData))
				.collect(toMap(AgentData::aid, mapToError::applyAsDouble));
	}

	@VisibleForTesting
	protected Map<String, Integer> getGreenSourcesWithPowerShortages(final Set<String> agentsOfInterest) {
		final ToIntFunction<AgentData> getPowerShortageCount = agentData ->
				((WeatherShortages) agentData.monitoringData()).weatherShortagesNumber();

		final Map<String, Integer> powerShortageMap = managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, agentsOfInterest.stream().toList(),
						MONITOR_SYSTEM_DATA_TIME_PERIOD).stream()
				.collect(groupingBy(AgentData::aid, TreeMap::new, summingInt(getPowerShortageCount)));

		return powerShortageMap.entrySet().stream()
				.filter(entry -> entry.getValue() >= POWER_SHORTAGE_THRESHOLD)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
