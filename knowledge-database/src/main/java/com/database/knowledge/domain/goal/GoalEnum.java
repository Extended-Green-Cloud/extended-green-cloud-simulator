package com.database.knowledge.domain.goal;

public enum GoalEnum {

	MAXIMIZE_JOB_SUCCESS_RATIO(new AdaptationGoal("Maximize job success ratio", 1)),
	MINIMIZE_USED_BACKUP_POWER(new AdaptationGoal("Minimize used backup power", 2)),
	DISTRIBUTE_TRAFFIC_EVENLY(new AdaptationGoal("Distribute traffic evenly", 3));

	public final AdaptationGoal adaptationGoal;

	GoalEnum(AdaptationGoal adaptationGoal) {
		this.adaptationGoal = adaptationGoal;
	}
}
