package org.greencloud.managingsystem.agent.behaviour.executor;

import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
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
	private final Instant executionTime;
	private final long executionDuration;
	private final Integer adaptationActionId;
	private final AID targetAgent;
	private final Map<GoalEnum, Double> initialGoalQualities;
	private final Runnable enablePlanAction;

	protected VerifyAdaptationActionResult(Agent agent, Instant executionTime, long executionDuration, int actionId,
			AID targetAgent, Map<GoalEnum, Double> initialGoalQualities, Runnable enablePlanAction,
			int delayInSeconds) {
		super(agent, delayInSeconds * 1000L);

		this.myManagingAgent = (ManagingAgent) agent;
		this.databaseClient = myManagingAgent.getAgentNode().getDatabaseClient();
		this.executionTime = executionTime;
		this.adaptationActionId = actionId;
		this.targetAgent = targetAgent;
		this.initialGoalQualities = initialGoalQualities;
		this.enablePlanAction = enablePlanAction;
		this.executionDuration = executionDuration;
	}

	/**
	 * Method creates verification behaviour for actions executed on singular agents.
	 * (with behaviour execution delay equals to VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS)
	 *
	 * @param agent                - agent executing the behaviour
	 * @param executionTime        - time when the adaptation was executed
	 * @param executionDuration    - time that it took for the action to be executed
	 * @param adaptationAction     -  executed action
	 * @param targetAgent          - agent on which adaptation was executed
	 * @param initialGoalQualities - values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     - method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 * @return VerifyAdaptationActionResult behaviour
	 */
	public static VerifyAdaptationActionResult createForAgentAction(Agent agent, Instant executionTime,
			long executionDuration, AdaptationAction adaptationAction, AID targetAgent,
			Map<GoalEnum, Double> initialGoalQualities, Runnable enablePlanAction) {
		final int actionId = adaptationAction.getActionId();
		return new VerifyAdaptationActionResult(agent, executionTime, executionDuration, actionId, targetAgent,
				initialGoalQualities, enablePlanAction, VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS);
	}

	/**
	 * Method creates verification behaviour for actions executed on entire system.
	 * (with behaviour execution delay equals to SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY)
	 *
	 * @param agent                - agent executing the behaviour
	 * @param adaptationAction     - executed action
	 * @param initialGoalQualities - values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     - method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 * @param executionDuration    - time that it took for the action to be executed
	 * @return VerifyAdaptationActionResult behaviour
	 */
	public static VerifyAdaptationActionResult createForSystemAction(Agent agent,
			AdaptationAction adaptationAction, Map<GoalEnum, Double> initialGoalQualities,
			Runnable enablePlanAction, long executionDuration) {
		final int actionId = adaptationAction.getActionId();
		return new VerifyAdaptationActionResult(agent, getCurrentTime(), executionDuration, actionId, null,
				initialGoalQualities, enablePlanAction, SYSTEM_ADAPTATION_PLAN_VERIFY_DELAY);
	}

	/**
	 * Method reads the result of adaptation action and updates the corresponding fields in the database
	 */
	@Override
	protected void onWake() {
		AdaptationAction performedAction = databaseClient.readAdaptationAction(adaptationActionId);
		logger.info(VERIFY_ACTION_START_LOG, performedAction, targetAgent, executionTime);

		var actionResults = getActionResults();
		var adaptationAction = databaseClient.updateAdaptationAction(performedAction.getActionId(), actionResults,
				executionDuration);
		myManagingAgent.getAgentNode().updateAdaptationAction(adaptationAction);
		enablePlanAction.run();

		logger.info(VERIFY_ACTION_END_LOG, performedAction, actionResults);
	}

	private Map<GoalEnum, Double> getActionResults() {
		return Arrays.stream(GoalEnum.values())
				.filter(goal -> initialGoalQualities.get(goal) != DATA_NOT_AVAILABLE_INDICATOR)
				.collect(toMap(goal -> goal, this::getGoalQualityDelta));
	}

	private double getGoalQualityDelta(GoalEnum goalEnum) {
		final int elapsedTime = (int) Duration.between(executionTime, getCurrentTime()).toSeconds();
		final double initialGoalQuality = initialGoalQualities.get(goalEnum);
		final double currentGoalQuality = myManagingAgent.monitor().getGoalService(goalEnum)
				.computeCurrentGoalQuality(elapsedTime);

		return currentGoalQuality - initialGoalQuality;
	}
}
