package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_AGENTS_ALIVE_TIME_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.greencloud.commons.managingsystem.planner.ImmutableIncrementGreenSourceErrorParameters;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of incrementing
 * the weather prediction error for given Green Source
 */
public class IncrementGreenSourceErrorPlan extends AbstractPlan {

	private static final int POWER_SHORTAGE_THRESHOLD = 1;
	private static final int MAXIMUM_PREDICTION_ERROR = 1;   // 1 is equivalent to 100%
	private Map<String, Integer> greenSourcesPowerShortages;

	public IncrementGreenSourceErrorPlan(ManagingAgent managingAgent) {
		super(INCREASE_GREEN_SOURCE_ERROR, managingAgent);
		this.greenSourcesPowerShortages = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. there are some GS alive in the system
	 * 2. there are some alive GS for which weather prediction error is less than 100%
	 * 3. there are some alive GS which had at least 1 power shortages per 5s
	 *
	 * @return boolean information if the plan is executable in current conditions
	 */
	@Override
	public boolean isPlanExecutable() {
		final List<AgentData> greenSourceData =
				managingAgent.getAgentNode().getDatabaseClient()
						.readMonitoringDataForDataTypes(List.of(HEALTH_CHECK, GREEN_SOURCE_MONITORING),
								MONITOR_AGENTS_ALIVE_TIME_PERIOD);
		final Map<String, Double> greenSourceErrorMap =
				getGreenSourcesWithErrors(greenSourceData, getAliveGreenSources(greenSourceData));

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
			return this;
		}
		final String selectedAgent = greenSourcesPowerShortages.entrySet().stream()
				.max(Comparator.comparingInt(Map.Entry::getValue))
				.orElseThrow()
				.getKey();
		targetAgent = new AID(selectedAgent, AID.ISGUID);
		actionParameters = ImmutableIncrementGreenSourceErrorParameters.builder().percentageChangeUnit(1).build();
		return this;
	}

	private List<String> getAliveGreenSources(List<AgentData> greenSourceData) {
		return greenSourceData.stream().filter(agentData -> agentData.dataType().equals(HEALTH_CHECK))
				.filter(agentData -> ((HealthCheck) agentData.monitoringData()).agentType().equals(GREEN_SOURCE))
				.map(AgentData::aid).toList();
	}

	private Map<String, Double> getGreenSourcesWithErrors(List<AgentData> greenSourceData, List<String> aliveAgents) {
		return greenSourceData.stream()
				.filter(agentData -> aliveAgents.contains(agentData.aid())
						&& agentData.dataType().equals(GREEN_SOURCE_MONITORING)
						&& ((GreenSourceMonitoringData) agentData.monitoringData()).getWeatherPredictionError()
						< MAXIMUM_PREDICTION_ERROR)
				.collect(toMap(AgentData::aid,
						agentData -> ((GreenSourceMonitoringData) agentData.monitoringData()).getWeatherPredictionError()));
	}

	private Map<String, Integer> getGreenSourcesWithPowerShortages(Set<String> agentsOfInterest) {
		final Map<String, List<Integer>> powerShortageMap = new HashMap<>();

		managingAgent.getAgentNode().getDatabaseClient()
				.readMonitoringDataForDataTypeAndAID(WEATHER_SHORTAGES, agentsOfInterest.stream().toList(),
						MONITOR_SYSTEM_DATA_TIME_PERIOD)
				.forEach(agentData -> putInPowerShortageMap(agentData, powerShortageMap));

		return powerShortageMap.entrySet().stream()
				.collect(toMap(Map.Entry::getKey,
						entry -> entry.getValue().stream().reduce(0, Integer::sum)))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > POWER_SHORTAGE_THRESHOLD)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private void putInPowerShortageMap(AgentData agentData, Map<String, List<Integer>> powerShortageMap) {
		final int shortageCount = ((WeatherShortages) agentData.monitoringData()).weatherShortagesNumber();
		final UnaryOperator<List<Integer>> putCountIntoMap = list -> {
			list.add(shortageCount);
			return list;
		};
		powerShortageMap.computeIfPresent(agentData.aid(), (agent, list) -> putCountIntoMap.apply(list));
		powerShortageMap.computeIfAbsent(agentData.aid(), aid -> new ArrayList<>());
	}

}
