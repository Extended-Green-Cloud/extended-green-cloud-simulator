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

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.google.common.collect.Maps;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.managingsystem.planner.ChangeGreenSourceWeights;

public class ChangeGreenSourceWeightPlan extends AbstractPlan {

	private final Map<String, Integer> greenSourceExecutedActions;
	private final Map<String, Integer> greenSourceAccumulatedShortages;
	private final Map<String, Integer> recentShortages;

	public ChangeGreenSourceWeightPlan(ManagingAgent managingAgent) {
		super(CHANGE_GREEN_SOURCE_WEIGHT, managingAgent);
		greenSourceExecutedActions = new HashMap<>();
		greenSourceAccumulatedShortages = new HashMap<>();
		recentShortages = new HashMap<>();
	}

	/**
	 * Plan is executable if any of monitoring servers reported any power shortages, both caused by
	 * physical or weather factors.
	 *
	 * @return result of the test
	 */
	@Override
	public boolean isPlanExecutable() {
		var readGreenSourceShortages = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(of(SHORTAGES))
				.stream()
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
	 * Picks the green source with the least number of executed actions that recently had a shortage.
	 *
	 * @return plan ready to be executed
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
			recentShortages.clear();
			return null;
		}

		targetAgent = search(managingAgent, SA_SERVICE_TYPE).stream()
				.filter(aid -> aid.toString().contains(targetServer))
				.findFirst()
				.orElse(null);
		recentShortages.clear();

		if (targetAgent == null) {
			return null;
		}

		return this;
	}

	private Map<String, Integer> getGreenSourceExecutedActionsForRecentShortages() {
		recentShortages.forEach((gsName, shortages) -> greenSourceExecutedActions.putIfAbsent(gsName, 0));
		return recentShortages.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> greenSourceExecutedActions.get(entry.getKey())));
	}
}
