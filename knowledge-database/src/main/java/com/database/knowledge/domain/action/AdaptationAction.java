package com.database.knowledge.domain.action;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import com.database.knowledge.types.GoalType;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.commons.enums.adaptation.AdaptationActionCategoryEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Object describing adaptation action that can be executed by the Managing Agent over Green Cloud
 */
@Getter
@AllArgsConstructor
public class AdaptationAction {

	private final Integer actionId;
	private final AdaptationActionTypeEnum action;
	private final GoalType goal;
	private final Map<GoalType, ActionResult> actionResults;
	private final Boolean isAvailable;
	private final AdaptationActionCategoryEnum type;
	private Integer runs;
	private Double executionDuration;

	public AdaptationAction(final Integer actionId, final AdaptationActionTypeEnum action,
			final AdaptationActionCategoryEnum type, final GoalType goal) {
		this.actionId = actionId;
		this.action = action;
		this.type = type;
		this.goal = goal;
		this.actionResults = stream(GoalType.values()).collect(toMap(g -> g, g -> new ActionResult()));
		this.isAvailable = true;
		this.runs = 0;
		this.executionDuration = 0.0;
	}

	/**
	 * Method returns the average differences in qualities associated with each of the actions
	 *
	 * @return map of actions along with corresponding quality differences
	 */
	public Map<GoalType, Double> getActionResultDifferences() {
		return actionResults.entrySet().stream().collect(toMap(Map.Entry::getKey, result -> result.getValue().diff()));
	}

	/**
	 * Merges adaptation actions results already present in the database with new data provided by the managing agent.
	 * NOT TO BE CALLED EXPLICITLY, method is only used by the Timescale Database internally when saving the object
	 * into database.
	 *
	 * @param newActionResults  new action results provided by Managing Agent when saved to database
	 * @param executionDuration execution duration of a given action
	 */
	public void mergeActionResults(final Map<GoalType, Double> newActionResults, final long executionDuration) {
		newActionResults.forEach((goalEnum, diff) -> {
			final ActionResult newResult = ofNullable(actionResults.get(goalEnum))
					.map(result -> new ActionResult(getUpdatedDiff(result, diff), result.runs() + 1))
					.orElse(new ActionResult(diff, 1));
			actionResults.put(goalEnum, newResult);
		});

		updateAvgExecutionDuration(executionDuration);
		runs += 1;
	}

	private void updateAvgExecutionDuration(final long newExecutionDuration) {
		executionDuration = runs == 0 ?
				newExecutionDuration :
				(runs * executionDuration + newExecutionDuration) / (runs + 1);
	}

	private double getUpdatedDiff(final ActionResult actionResult, final Double newDiff) {
		return (actionResult.diff() * actionResult.runs() + newDiff) / (actionResult.runs() + 1);
	}

	@Override
	public String toString() {
		return action.name();
	}
}
