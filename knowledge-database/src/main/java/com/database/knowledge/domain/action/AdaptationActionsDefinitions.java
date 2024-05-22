package com.database.knowledge.domain.action;

import static org.greencloud.commons.enums.adaptation.AdaptationActionCategoryEnum.ADD_COMPONENT;
import static org.greencloud.commons.enums.adaptation.AdaptationActionCategoryEnum.RECONFIGURE;
import static org.greencloud.commons.enums.adaptation.AdaptationActionCategoryEnum.REMOVE_COMPONENT;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.ADD_GREEN_SOURCE;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.ADD_SERVER;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.CONNECT_GREEN_SOURCE;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.DECREASE_GREEN_SOURCE_ERROR;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.DISABLE_SERVER;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.DISCONNECT_GREEN_SOURCE;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.ENABLE_SERVER;
import static org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.types.GoalType.DISTRIBUTE_TRAFFIC_EVENLY;
import static com.database.knowledge.types.GoalType.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.types.GoalType.MINIMIZE_USED_BACKUP_POWER;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.args.adaptation.singleagent.AdjustGreenSourceErrorParameters;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceConnectionParameters;
import org.greencloud.commons.args.adaptation.singleagent.ChangeGreenSourceWeights;
import org.greencloud.commons.args.adaptation.singleagent.DisableServerActionParameters;
import org.greencloud.commons.args.adaptation.singleagent.EnableServerActionParameters;
import org.greencloud.commons.exception.InvalidAdaptationActionException;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import com.database.knowledge.types.GoalType;

/**
 * Definitions provider for each of the adaptation actions. Used internally by the Timescale Database when initializing
 * the tables.
 */
public final class AdaptationActionsDefinitions {

	private static final List<AdaptationAction> ADAPTATION_ACTIONS = List.of(
			// SUCCESS RATIO
			new AdaptationAction(1, ADD_SERVER, ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(4, CHANGE_GREEN_SOURCE_WEIGHT, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(5, INCREASE_GREEN_SOURCE_ERROR, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(6, CONNECT_GREEN_SOURCE, ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
			new AdaptationAction(12, ENABLE_SERVER, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
			// MINIMIZE BACKUP POWER
			new AdaptationAction(7, DECREASE_GREEN_SOURCE_ERROR, RECONFIGURE, MINIMIZE_USED_BACKUP_POWER),
			new AdaptationAction(8, ADD_GREEN_SOURCE, ADD_COMPONENT, MINIMIZE_USED_BACKUP_POWER),
			// DISTRIBUTE TRAFFIC EVENLY
			new AdaptationAction(9, DISCONNECT_GREEN_SOURCE, REMOVE_COMPONENT, DISTRIBUTE_TRAFFIC_EVENLY),
			new AdaptationAction(10, DISABLE_SERVER, RECONFIGURE, DISTRIBUTE_TRAFFIC_EVENLY),
			new AdaptationAction(11, ENABLE_SERVER, RECONFIGURE, DISTRIBUTE_TRAFFIC_EVENLY)
	);

	private static final Map<AdaptationActionTypeEnum, Class<? extends AdaptationActionParameters>> ACTION_TO_PARAMS_MAP =
			Map.of(
					INCREASE_GREEN_SOURCE_ERROR, AdjustGreenSourceErrorParameters.class,
					DECREASE_GREEN_SOURCE_ERROR, AdjustGreenSourceErrorParameters.class,
					CONNECT_GREEN_SOURCE, ChangeGreenSourceConnectionParameters.class,
					DISCONNECT_GREEN_SOURCE, ChangeGreenSourceConnectionParameters.class,
					CHANGE_GREEN_SOURCE_WEIGHT, ChangeGreenSourceWeights.class,
					DISABLE_SERVER, DisableServerActionParameters.class,
					ENABLE_SERVER, EnableServerActionParameters.class
			);

	private AdaptationActionsDefinitions() {
	}

	public static List<AdaptationAction> getAdaptationActions() {
		return ADAPTATION_ACTIONS;
	}

	public static List<AdaptationAction> getAdaptationAction(final AdaptationActionTypeEnum action) {
		if (ADAPTATION_ACTIONS.stream().noneMatch(val -> val.getAction().equals(action))) {
			throw new InvalidAdaptationActionException(action.getName());
		}

		return ADAPTATION_ACTIONS.stream()
				.filter(val -> val.getAction().equals(action))
				.toList();
	}

	public static AdaptationAction getAdaptationAction(final AdaptationActionTypeEnum action, final GoalType goalEnum) {
		return ADAPTATION_ACTIONS.stream()
				.filter(val -> val.getAction().equals(action) && val.getGoal().equals(goalEnum))
				.findFirst().orElseThrow(() -> new InvalidAdaptationActionException(action.getName()));
	}

	public static Class<? extends AdaptationActionParameters> getActionParametersClass(
			AdaptationActionTypeEnum adaptationActionEnum) {
		return ACTION_TO_PARAMS_MAP.get(adaptationActionEnum);
	}
}
