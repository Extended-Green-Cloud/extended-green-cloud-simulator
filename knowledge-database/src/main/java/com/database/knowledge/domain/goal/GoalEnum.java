package com.database.knowledge.domain.goal;

import java.util.Arrays;

import com.database.knowledge.exception.InvalidGoalIdentifierException;

public enum GoalEnum {

	MAXIMIZE_JOB_SUCCESS_RATIO(1),
	MINIMIZE_USED_BACKUP_POWER(2),
	DISTRIBUTE_TRAFFIC_EVENLY(3);

	public final int adaptationGoalId;

	GoalEnum(int adaptationGoalId) {
		this.adaptationGoalId = adaptationGoalId;
	}

	public static GoalEnum getByGoalId(final int adaptationGoalId) {
		return Arrays.stream(values()).
				filter(goal -> adaptationGoalId == goal.adaptationGoalId)
				.findFirst()
				.orElseThrow(() -> new InvalidGoalIdentifierException(adaptationGoalId));
	}

	public int getAdaptationGoalId() {
		return adaptationGoalId;
	}
}
