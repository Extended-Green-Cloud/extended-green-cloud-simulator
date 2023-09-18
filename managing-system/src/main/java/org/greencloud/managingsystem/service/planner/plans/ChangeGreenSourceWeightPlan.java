package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static org.greencloud.commons.args.agent.AgentType.GREEN_ENERGY;
import static java.util.Collections.min;
import static java.util.Comparator.comparingInt;
import static java.util.List.of;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.domain.goal.GoalEnum;
import com.google.common.collect.Maps;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceWeights;

import jade.core.AID;

/**
 * Class containing adaptation plan which realizes the action of changing weight of selection of new green source
 */
public class ChangeGreenSourceWeightPlan extends AbstractPlan {

	static final Map<String, Integer> greenSourceExecutedActions = new HashMap<>();
	static final Map<String, Integer> greenSourceAccumulatedShortages = new HashMap<>();

	private final Map<String, Integer> recentShortages;

	public ChangeGreenSourceWeightPlan(ManagingAgent managingAgent, GoalEnum violatedGoal) {
		super(CHANGE_GREEN_SOURCE_WEIGHT, managingAgent, violatedGoal);
		recentShortages = new HashMap<>();
	}

	/**
	 * Method verifies if the plan is executable. The plan is executable if:
	 * 1. any of the green sources reported any power shortages, both caused by physical or weather factors
	 * 2. there are some green sources connected to at least 1 active server (i.e. if the green source is currently
	 * connected to the servers that are being disabled - such source should not be taken under consideration)
	 *
	 * @return boolean value indicating if the plan is executable
	 */
	@Override
	public boolean isPlanExecutable() {
		var readGreenSourceShortages = getGreenSourceData();

		if (readGreenSourceShortages.isEmpty()) {
			// if there are no alive green sources
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
				.map(GreenEnergyArgs::getOwnerSever)
				.findFirst()
				.orElse(null);

		if (targetServer == null) {
			return null;
		}

		final List<String> aliveServers = managingAgent.monitor().getActiveServers();
		final String targetServerAID = aliveServers.stream()
				.filter(aid -> aid.split("@")[0].equals(targetServer))
				.findFirst()
				.orElse(null);

		if (targetServerAID == null) {
			return null;
		}

		targetAgent = new AID(targetServerAID, AID.ISGUID);
		return this;
	}

	private Map<String, Integer> getGreenSourceData() {
		final List<AgentData> agentsWithShortages = managingAgent.getAgentNode().getDatabaseClient()
				.readLastMonitoringDataForDataTypes(of(SHORTAGES)).stream()
				.filter(filterAliveGreenEnergyAgents())
				.toList();
		final Map<String, Integer> allAgentsWithShortages = managingAgent.monitor()
				.concatLatestAgentDataWithNotRegistered(agentsWithShortages, GREEN_ENERGY,
						data -> ((Shortages) data.monitoringData()).shortages(), 0);

		return allAgentsWithShortages.entrySet().stream()
				.filter(entry -> filterGreenSourcesConnectedToActiveServer().test(entry.getKey()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Predicate<AgentData> filterAliveGreenEnergyAgents() {
		final List<String> aliveGreenSources = managingAgent.monitor().getAliveAgents(GREEN_ENERGY);
		return data -> aliveGreenSources.stream().anyMatch(greenSource -> data.aid().contains(greenSource));
	}

	private Predicate<String> filterGreenSourcesConnectedToActiveServer() {
		final List<String> activeServers = managingAgent.monitor().getActiveServers();
		return name -> managingAgent.getGreenCloudStructure().getServersConnectedToGreenSource(name)
				.stream()
				.anyMatch(activeServers::contains);
	}

	private Map<String, Integer> getGreenSourceExecutedActionsForRecentShortages() {
		recentShortages.forEach((gsName, shortages) -> greenSourceExecutedActions.putIfAbsent(gsName, 0));
		return recentShortages.entrySet().stream()
				.collect(toMap(Map.Entry::getKey, entry -> greenSourceExecutedActions.get(entry.getKey())));
	}
}
