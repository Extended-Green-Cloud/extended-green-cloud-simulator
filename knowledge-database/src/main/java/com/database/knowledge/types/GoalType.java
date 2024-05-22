package com.database.knowledge.types;

import static java.util.Arrays.stream;

import org.greencloud.commons.exception.InvalidGoalIdentifierException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoalType {

	MAXIMIZE_JOB_SUCCESS_RATIO(1),
	MINIMIZE_USED_BACKUP_POWER(2),
	DISTRIBUTE_TRAFFIC_EVENLY(3);

	public final int adaptationGoalId;

	public static GoalType getByGoalId(final int adaptationGoalId) {
		return stream(values()).
				filter(goal -> adaptationGoalId == goal.adaptationGoalId)
				.findFirst()
				.orElseThrow(() -> new InvalidGoalIdentifierException(adaptationGoalId));
	}

}
