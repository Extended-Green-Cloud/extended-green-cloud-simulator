package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.VERIFY_ACTION_END_LOG;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.VERIFY_ACTION_START_LOG;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Measures delta of each Goal's qualities for a time period starting from action execution to now().
 * After measuring results of the {@link AdaptationAction} they are aggregated and stored in database.
 * Aggregation and update is done by the {@link TimescaleDatabase} client. Final action is to enable
 * back the verified action back.
 */
public class VerifyAdaptationActionResult extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(VerifyAdaptationActionResult.class);

	private final ManagingAgent myManagingAgent;
	private final TimescaleDatabase databaseClient;
	private final Instant actionTimestamp;
	private final Integer adaptationActionId;
	private final AID targetAgent;
	private final Double initialGoalQuality;

	public VerifyAdaptationActionResult(Agent agent, Instant actionTimestamp, AdaptationActionEnum adaptationActionType,
			AID targetAgent, Double initialGoalQuality) {
		super(agent, VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS * 1000L);
		this.myManagingAgent = (ManagingAgent) agent;
		this.databaseClient = myManagingAgent.getAgentNode().getDatabaseClient();
		this.actionTimestamp = actionTimestamp;
		this.adaptationActionId = getAdaptationAction(adaptationActionType).getActionId();
		this.targetAgent = targetAgent;
		this.initialGoalQuality = initialGoalQuality;
	}

	@Override
	protected void onWake() {
		AdaptationAction performedAction = databaseClient.readAdaptationAction(adaptationActionId);
		logger.info(VERIFY_ACTION_START_LOG, performedAction, targetAgent, actionTimestamp);

		var actionResults = getActionResults(performedAction);
		databaseClient.updateAdaptationAction(performedAction.getActionId(), actionResults);
		enableAdaptationAction(performedAction);

		logger.info(VERIFY_ACTION_END_LOG, performedAction, actionResults);
	}

	private Map<GoalEnum, Double> getActionResults(AdaptationAction performedAction) {
		// TODO iterate over each GoalEnum value when all GoalServices are implemented
		return Map.of(
				performedAction.getGoal(), getGoalQualityDelta(performedAction.getGoal()),
				GoalEnum.MINIMIZE_USED_BACKUP_POWER, 0.0,
				GoalEnum.DISTRIBUTE_TRAFFIC_EVENLY, 0.0
		);
	}

	private double getGoalQualityDelta(GoalEnum goalEnum) {
		int elapsedTime = (int) Duration.between(actionTimestamp, getCurrentTime()).toSeconds();
		double currentGoalQuality = myManagingAgent.monitor().getGoalService(goalEnum)
				.readCurrentGoalQuality(elapsedTime);

		// absolute delta
		return initialGoalQuality - currentGoalQuality;
	}

	private void enableAdaptationAction(AdaptationAction adaptationAction) {
		if (adaptationAction.getAction() == AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY) {
			myManagingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(
							getAdaptationAction(AdaptationActionEnum.INCREASE_POWER_PRIORITY).getActionId(), true);
		} else if (adaptationAction.getAction() == AdaptationActionEnum.INCREASE_POWER_PRIORITY) {
			myManagingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(
							getAdaptationAction(AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY).getActionId(), true);
		}
		myManagingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(adaptationAction.getActionId(), true);
	}
}
