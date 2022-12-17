package com.database.knowledge.domain.action;

import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_PERCENTAGE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionTypeEnum.ADD_COMPONENT;
import static com.database.knowledge.domain.action.AdaptationActionTypeEnum.RECONFIGURE;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;

import java.util.List;
import java.util.Map;

import com.database.knowledge.exception.InvalidAdaptationActionException;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.managingsystem.planner.AdjustGreenSourceErrorParameters;
import com.greencloud.commons.managingsystem.planner.ConnectGreenSourceParameters;

/**
 * Definitions provider for each of the adaptation actions. Used internally by the Timescale Database when initializing
 * the tables.
 */
public final class AdaptationActionsDefinitions {

	private static final List<AdaptationAction> ADAPTATION_ACTIONS = List.of(
			new AdaptationAction(1, ADD_SERVER,
					ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(2, INCREASE_DEADLINE_PRIORITY,
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(3, INCREASE_POWER_PRIORITY,
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(4, INCREASE_GREEN_SOURCE_PERCENTAGE,
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(5, INCREASE_GREEN_SOURCE_ERROR,
					RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(7, CONNECT_GREEN_SOURCE,
					ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(8, DECREASE_GREEN_SOURCE_ERROR,
					RECONFIGURE, MINIMIZE_USED_BACKUP_POWER)
	);

	private static final Map<AdaptationActionEnum, Class<? extends AdaptationActionParameters>> ACTION_TO_PARAMS_MAP =
			Map.of(
					INCREASE_GREEN_SOURCE_ERROR, AdjustGreenSourceErrorParameters.class,
					DECREASE_GREEN_SOURCE_ERROR, AdjustGreenSourceErrorParameters.class,
					CONNECT_GREEN_SOURCE, ConnectGreenSourceParameters.class
			);

	private AdaptationActionsDefinitions() {
	}

	public static List<AdaptationAction> getAdaptationActions() {
		return ADAPTATION_ACTIONS;
	}

	public static AdaptationAction getAdaptationAction(final AdaptationActionEnum action) {
		return ADAPTATION_ACTIONS.stream()
				.filter(val -> val.getAction().equals(action))
				.findFirst().orElseThrow(() -> new InvalidAdaptationActionException(action.getName()));
	}

	public static Class<? extends AdaptationActionParameters> getActionParametersClass(
			AdaptationActionEnum adaptationActionEnum) {
		return ACTION_TO_PARAMS_MAP.get(adaptationActionEnum);
	}
}
