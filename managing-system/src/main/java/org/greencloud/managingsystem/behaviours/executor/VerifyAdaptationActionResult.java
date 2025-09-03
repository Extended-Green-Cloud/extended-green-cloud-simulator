package org.greencloud.managingsystem.behaviours.executor;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.MonitoringConstants.DATA_NOT_AVAILABLE_INDICATOR;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.database.knowledge.types.GoalType;

import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour verifies the results of performed adaptation and updates the statistics in the database.
 */
public class VerifyAdaptationActionResult extends WakerBehaviour {

	private static final LogBuilder log = constructLogBuilder(VerifyAdaptationActionResult.class);

	private final ManagingAgent myManagingAgent;
	private final TimescaleDatabase database;
	private final Instant executionTime;
	private final long executionDuration;
	private final Integer adaptationActionId;
	private final AID targetAgent;
	private final Map<GoalType, Double> initialGoalQualities;
	private final Runnable enablePlanAction;

	/**
	 * Behaviour constructor (for non-system adaptations).
	 *
	 * @param agent                agent executing the behaviour
	 * @param executionTime        time when the adaptation was executed
	 * @param executionDuration    time that it took for the action to be executed
	 * @param action               executed action
	 * @param targetAgent          agent on which adaptation was executed
	 * @param initialGoalQualities values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 */
	public VerifyAdaptationActionResult(final ManagingAgent agent,
			final Instant executionTime,
			final long executionDuration,
			final AdaptationAction action,
			final AID targetAgent,
			final Map<GoalType, Double> initialGoalQualities,
			final Runnable enablePlanAction) {
		super(agent, agent.getProperties().getConfig().getComponentAdaptationVerificationDelay() * 1000L);

		this.myManagingAgent = agent;
		this.database = myManagingAgent.getAgentNode().getDatabaseClient();
		this.executionTime = executionTime;
		this.adaptationActionId = action.getActionId();
		this.targetAgent = targetAgent;
		this.initialGoalQualities = initialGoalQualities;
		this.enablePlanAction = enablePlanAction;
		this.executionDuration = executionDuration;
	}

	/**
	 * Behaviour constructor (for system adaptations).
	 *
	 * @param agent                agent executing the behaviour
	 * @param executionTime        time when the adaptation was executed
	 * @param executionDuration    time that it took for the action to be executed
	 * @param action               executed action
	 * @param initialGoalQualities values of the goal qualities before performing the adaptation
	 * @param enablePlanAction     method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 */
	public VerifyAdaptationActionResult(final ManagingAgent agent,
			final Instant executionTime,
			final long executionDuration,
			final AdaptationAction action,
			final Map<GoalType, Double> initialGoalQualities,
			final Runnable enablePlanAction) {
		super(agent, agent.getProperties().getConfig().getSystemAdaptationVerificationDelay() * 1000L);

		this.targetAgent = null;
		this.myManagingAgent = agent;
		this.database = myManagingAgent.getAgentNode().getDatabaseClient();
		this.executionTime = executionTime;
		this.adaptationActionId = action.getActionId();
		this.initialGoalQualities = initialGoalQualities;
		this.enablePlanAction = enablePlanAction;
		this.executionDuration = executionDuration;
	}

	/**
	 * Method reads the result of adaptation action and updates the corresponding fields in the database
	 */
	@Override
	protected void onWake() {
		final AdaptationAction performedAction = database.readAdaptationAction(adaptationActionId);
		final Map<GoalType, Double> actionResults = getActionResults();

		log.messageInfo("Verifying {} executed on {} at {}.", performedAction, targetAgent, executionTime);

		updateActionState(actionResults, performedAction.getActionId());
		enablePlanAction.run();

		log.messageInfo("Action {} verified with following goal changes: {}", performedAction, actionResults);
	}

	private void updateActionState(final Map<GoalType, Double> actionResults, final int actionId) {
		final AdaptationAction actionState = database.updateAdaptationAction(actionId, actionResults,
				executionDuration);
		myManagingAgent.getAgentNode().updateAdaptationAction(actionState);
	}

	private Map<GoalType, Double> getActionResults() {
		return Arrays.stream(GoalType.values())
				.filter(goal -> initialGoalQualities.get(goal) != DATA_NOT_AVAILABLE_INDICATOR)
				.collect(toMap(goal -> goal, this::getGoalQualityDelta));
	}

	private double getGoalQualityDelta(GoalType goalEnum) {
		final int elapsedTime = (int) Duration.between(executionTime, getCurrentTime()).toSeconds();
		final double initialGoalQuality = initialGoalQualities.get(goalEnum);
		final double currentGoalQuality = myManagingAgent.monitor().getGoalService(goalEnum)
				.computeCurrentGoalQuality(elapsedTime);

		return currentGoalQuality - initialGoalQuality;
	}
}
