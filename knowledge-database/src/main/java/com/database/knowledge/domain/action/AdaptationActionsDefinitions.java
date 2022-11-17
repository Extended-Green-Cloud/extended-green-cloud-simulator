package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_COMPONENT;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;

import java.util.List;

/**
 * Definitions provider for each of the adaptation actions. Used internally by the Timescale Database when initializing
 * the tables.
 */
public final class AdaptationActionsDefinitions {

	private static final List<AdaptationAction> ADAPTATION_ACTIONS = List.of(
			new AdaptationAction(1, "Add server", ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO)
	);

	private AdaptationActionsDefinitions() {
	}

	public static List<AdaptationAction> getAdaptationActions() {
		return ADAPTATION_ACTIONS;
	}
}
