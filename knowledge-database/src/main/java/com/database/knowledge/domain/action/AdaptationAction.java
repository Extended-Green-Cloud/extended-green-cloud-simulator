package com.database.knowledge.domain.action;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.database.knowledge.domain.goal.GoalEnum;

/**
 * Object describing adaptation action that can be executed by the Managing Agent over Green Cloud
 */
public class AdaptationAction {

	private final Integer actionId;
	private final String actionName;
	private final GoalEnum goal;
	// describes delta for each adaptation goal value that given action caused
	private final Map<GoalEnum, Double> actionResults;
	private final Boolean isAvailable;
	private Integer runs;
	private AdaptationActionEnum type;

	public AdaptationAction(Integer actionId, String actionName, AdaptationActionEnum type, GoalEnum goal) {
		this.actionId = actionId;
		this.actionName = actionName;
		this.type = type;
		this.goal = goal;
		this.actionResults = Arrays.stream(GoalEnum.values())
				.collect(Collectors.toMap(goalEnum -> goalEnum, goalEnum -> 0.0D));
		this.isAvailable = true;
		this.runs = 0;
	}

	public AdaptationAction(Integer actionId, String actionName, AdaptationActionEnum type, GoalEnum goal, Map<GoalEnum, Double> actionResults,
			Boolean isAvailable, Integer runs) {
		this.actionId = actionId;
		this.actionName = actionName;
		this.type = type;
		this.goal = goal;
		this.isAvailable = isAvailable;
		this.actionResults = actionResults;
		this.runs = runs;
	}

	public Integer getActionId() {
		return actionId;
	}

	public String getActionName() {
		return actionName;
	}

	public GoalEnum getGoal() {
		return goal;
	}

	public Map<GoalEnum, Double> getActionResults() {
		return actionResults;
	}

	/**
	 * Merges adaptation actions results already present in the database with new data provided by the managing agent.
	 * NOT TO BE CALLED EXPLICITLY, method is only used by the Timescale Database internally when saving the object
	 * into database.
	 *
	 * @param newActionResults new action results provided by Managing Agent when saved to database
	 */
	public void mergeActionResults(Map<GoalEnum, Double> newActionResults) {
		Arrays.stream(GoalEnum.values()).forEach(goalEnum -> {
			if (runs == 0) {
				actionResults.put(goalEnum, newActionResults.get(goalEnum));
			} else {
				actionResults.put(goalEnum, getUpdatedGoalChange(goalEnum, newActionResults));
			}
		});
	}

	public Boolean getAvailable() {
		return isAvailable;
	}

	public Integer getRuns() {
		return runs;
	}

	public AdaptationActionEnum getType() {
		return type;
	}

	/**
	 * Updates the number of how many times action was run.
	 * NOT TO BE CALLED EXPLICITLY, method is only used by the Timescale Database internally when saving the object
	 * into database
	 */
	public void increaseRuns() {
		this.runs += 1;
	}

	private double getUpdatedGoalChange(GoalEnum goal, Map<GoalEnum, Double> newActionResults) {
		return (actionResults.get(goal) * runs + newActionResults.get(goal)) / (runs + 1);
	}
}
