package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_COMPONENT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.RECONFIGURE;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;

import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Definitions provider for each of the adaptation actions. Used internally by the Timescale Database when initializing
 * the tables.
 */
public final class AdaptationActionsDefinitions {

	private static final List<AdaptationAction> ADAPTATION_ACTIONS = List.of(
			new AdaptationAction(1, "Add Server",
					ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(2, "Increase job deadline priority",
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(3, "Increase job power priority",
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(4, "Increase Green Source selection chance",
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(5, "Increase Green Source weather prediction error",
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(7, "Add Green Source",
					ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO)
	);

	private AdaptationActionsDefinitions() {
	}

	public static List<AdaptationAction> getAdaptationActions() {
		return ADAPTATION_ACTIONS;
	}

	@Nullable
	public static AdaptationAction getAdaptationActionById(Integer id) {
		return ADAPTATION_ACTIONS.stream()
				.filter(action -> action.getActionId().equals(id))
				.findFirst()
				.orElse(null);
	}
}
