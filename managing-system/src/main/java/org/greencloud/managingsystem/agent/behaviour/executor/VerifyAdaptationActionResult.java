package org.greencloud.managingsystem.agent.behaviour.executor;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.VERIFY_ACTION_END_LOG;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.VERIFY_ACTION_START_LOG;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
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
 * Behaviour verifies the results of performed adaptation and updates the statistics in the database.
 */
public class VerifyAdaptationActionResult extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(VerifyAdaptationActionResult.class);

	private final ManagingAgent myManagingAgent;
	private final TimescaleDatabase databaseClient;
	private final Instant actionExecutionTime;
	private final Integer adaptationActionId;
	private final AID targetAgent;
	private final Map<GoalEnum, Double> initialGoalQualities;
	private final Runnable enablePlanAction;

	protected VerifyAdaptationActionResult(Agent agent, Instant actionExecutionTime,
			AdaptationActionEnum adaptationActionType, AID targetAgent, Map<GoalEnum, Double> initialGoalQualities,
			Runnable enablePlanAction, int delayInSeconds) {
		super(agent, delayInSeconds * 1000L);

		this.myManagingAgent = (ManagingAgent) agent;
		this.databaseClient = myManagingAgent.getAgentNode().getDatabaseClient();
		this.actionExecutionTime = actionExecutionTime;
		this.adaptationActionId = getAdaptationAction(adaptationActionType).getActionId();
		this.targetAgent = targetAgent;
		this.initialGoalQualities = initialGoalQualities;
		this.enablePlanAction = enablePlanAction;
	}

	/**
	 * Method creates verification behaviour for actions executed on singular agents.
	 * (with behaviour execution delay equals to VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS)
	 *
	 * @param agent                - agent executing the behaviour
	 * @param actionExecutionTime  - time when the adaptation was executed
	 * @param adaptationActionType - type of the executed action
	 * @param targetAgent          - agent on which adaptation was executed
	 * @param initialGoalQualities - values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     - method performed in order to enable availability of an action corresponding to
	 *                             *                           given plan
	 * @return VerifyAdaptationActionResult behaviour
	 */
	public static VerifyAdaptationActionResult createForAgentAction(Agent agent, Instant actionExecutionTime,
			AdaptationActionEnum adaptationActionType, AID targetAgent, Map<GoalEnum, Double> initialGoalQualities,
			Runnable enablePlanAction) {
		return new VerifyAdaptationActionResult(agent, actionExecutionTime, adaptationActionType, targetAgent,
				initialGoalQualities, enablePlanAction, VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS);
	}

	/**
	 * Method creates verification behaviour for actions executed on entire system.
	 * (with behaviour execution delay equals to SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY)
	 *
	 * @param agent                - agent executing the behaviour
	 * @param adaptationActionType - type of the executed action
	 * @param initialGoalQualities - values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     - method performed in order to enable availability of an action corresponding to
	 *                             *                           given plan
	 * @return VerifyAdaptationActionResult behaviour
	 */
	public static VerifyAdaptationActionResult createForSystemAction(Agent agent,
			AdaptationActionEnum adaptationActionType, Map<GoalEnum, Double> initialGoalQualities,
			Runnable enablePlanAction) {
		return new VerifyAdaptationActionResult(agent, getCurrentTime(), adaptationActionType, null,
				initialGoalQualities, enablePlanAction, SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY);
	}

	/**
	 * Method reads the result of adaptation action and updates the corresponding fields in the database
	 */
	@Override
	protected void onWake() {
		AdaptationAction performedAction = databaseClient.readAdaptationAction(adaptationActionId);
		logger.info(VERIFY_ACTION_START_LOG, performedAction, targetAgent, actionExecutionTime);

		var actionResults = getActionResults();
		databaseClient.updateAdaptationAction(performedAction.getActionId(), actionResults);
		enablePlanAction.run();

		logger.info(VERIFY_ACTION_END_LOG, performedAction, actionResults);
	}

	private Map<GoalEnum, Double> getActionResults() {
		return Arrays.stream(GoalEnum.values()).collect(toMap(goal -> goal, this::getGoalQualityDelta));
	}

	private double getGoalQualityDelta(GoalEnum goalEnum) {
		final int elapsedTime = (int) Duration.between(actionExecutionTime, getCurrentTime()).toSeconds();
		final double currentGoalQuality = myManagingAgent.monitor().getGoalService(goalEnum)
				.computeCurrentGoalQuality(elapsedTime);

		return currentGoalQuality - initialGoalQualities.get(goalEnum);
	}
}
