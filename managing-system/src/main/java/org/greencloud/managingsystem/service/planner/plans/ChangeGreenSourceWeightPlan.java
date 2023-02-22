package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static com.greencloud.application.yellowpages.YellowPagesService.search;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.SA_SERVICE_TYPE;
import static java.util.Collections.min;
import static java.util.Comparator.comparingInt;
import static java.util.List.of;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.google.common.collect.Maps;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceWeights;

/**
 * Class containing adaptation plan which realizes the action of changing weight of selection of new green source
 */
public class ChangeGreenSourceWeightPlan extends AbstractPlan {

	static final Map<String, Integer> greenSourceExecutedActions = new HashMap<>();
	static final Map<String, Integer> greenSourceAccumulatedShortages = new HashMap<>();

	private final Map<String, Integer> recentShortages;

	public ChangeGreenSourceWeightPlan(ManagingAgent managingAgent) {
		super(CHANGE_GREEN_SOURCE_WEIGHT, managingAgent);
		recentShortages = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. any of the green sources reported any power shortages, both caused by physical or weather factors
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		var readGreenSourceShortages = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(of(SHORTAGES))
				.stream()
				.filter(filterAliveGreenEnergyAgents())
				.collect(toMap(AgentData::aid, data -> ((Shortages) data.monitoringData()).shortages()));

		if (readGreenSourceShortages.isEmpty()) {
			return false;
		}

		if (greenSourceAccumulatedShortages.isEmpty()) {
			// just populate the maps on the first run
			greenSourceAccumulatedShortages.putAll(readGreenSourceShortages);
			recentShortages.putAll(readGreenSourceShortages);
			return true;
		}

		var mapDiff = Maps.difference(greenSourceAccumulatedShortages, readGreenSourceShortages);
		if (mapDiff.areEqual()) {
			// do nothing if no power shortages occurred since last run
			return false;
		}

		// if the maps differ update the accumulated shortages and populate map of recent shortages
		recentShortages.putAll(mapDiff.entriesOnlyOnRight());
		mapDiff.entriesDiffering().forEach((greenSourceName, shortagesDifference) -> {
			var accumulatedShortages = shortagesDifference.rightValue();
			greenSourceAccumulatedShortages.replace(greenSourceName, accumulatedShortages);
			recentShortages.put(greenSourceName, accumulatedShortages);
		});

		return true;
	}

	/**
	 * Method constructs plan which changes the weight of selection of a given green source.
	 * The green source that is picked has the least number of executed actions that recently had a shortage.
	 *
	 * @return prepared adaptation plan
	 */
	@Override
	public AbstractPlan constructAdaptationPlan() {
		var targetGreenSource = min(getGreenSourceExecutedActionsForRecentShortages().entrySet(),
				comparingInt(Map.Entry::getValue));
		greenSourceExecutedActions.replace(targetGreenSource.getKey(), targetGreenSource.getValue() + 1);
		actionParameters = new ChangeGreenSourceWeights(targetGreenSource.getKey());
		var targetServer = managingAgent.getGreenCloudStructure().getGreenEnergyAgentsArgs().stream()
				.filter(args -> targetGreenSource.getKey().contains(args.getName()))
				.map(GreenEnergyAgentArgs::getOwnerSever)
				.findFirst()
				.orElse(null);

		if (targetServer == null) {
			return null;
		}

		targetAgent = search(managingAgent, SA_SERVICE_TYPE).stream()
				.filter(aid -> aid.toString().contains(targetServer))
				.findFirst()
				.orElse(null);

		if (targetAgent == null) {
			return null;
		}

		return this;
	}

	private Predicate<AgentData> filterAliveGreenEnergyAgents() {
		return data -> managingAgent.getGreenCloudStructure()
				.getGreenEnergyAgentsArgs()
				.stream()
				.anyMatch(args -> data.aid().contains(args.getName()));
	}

	private Map<String, Integer> getGreenSourceExecutedActionsForRecentShortages() {
		recentShortages.forEach((gsName, shortages) -> greenSourceExecutedActions.putIfAbsent(gsName, 0));
		return recentShortages.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> greenSourceExecutedActions.get(entry.getKey())));
	}
}
