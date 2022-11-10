package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.goal.GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;

import java.util.Map;
import java.util.stream.IntStream;

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

	public AdaptationAction(Integer actionId, String actionName, GoalEnum goal) {
		this.actionId = actionId;
		this.actionName = actionName;
		this.goal = goal;
		this.actionResults = Map.of(
				MAXIMIZE_JOB_SUCCESS_RATIO, 0.0,
				MINIMIZE_USED_BACKUP_POWER, 0.0,
				DISTRIBUTE_TRAFFIC_EVENLY, 0.0
		);
		this.isAvailable = true;
		this.runs = 0;
	}

	public AdaptationAction(Integer actionId, String actionName, GoalEnum goal, Map<GoalEnum, Double> actionResults,
			Boolean isAvailable, Integer runs) {
		this.actionId = actionId;
		this.actionName = actionName;
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
		IntStream.range(MAXIMIZE_JOB_SUCCESS_RATIO.ordinal(), DISTRIBUTE_TRAFFIC_EVENLY.ordinal() + 1)
				.forEach(i -> {
					if (runs == 0) {
						actionResults.put(GoalEnum.values()[i], newActionResults.get(GoalEnum.values()[i]));
					} else {
						actionResults.put(GoalEnum.values()[i], getUpdatedGoalChange(i, newActionResults));
					}
				});
	}

	public Boolean getAvailable() {
		return isAvailable;
	}

	public Integer getRuns() {
		return runs;
	}

	/**
	 * Updates the number of how many times action was run.
	 * NOT TO BE CALLED EXPLICITLY, method is only used by the Timescale Database internally when saving the object
	 * into database
	 */
	public void increaseRuns() {
		this.runs += 1;
	}

	private double getUpdatedGoalChange(int goalId, Map<GoalEnum, Double> newActionResults) {
		return (actionResults.get(GoalEnum.values()[goalId]) * runs + newActionResults.get(GoalEnum.values()[goalId]))
			   / (runs + 1);
	}
}
