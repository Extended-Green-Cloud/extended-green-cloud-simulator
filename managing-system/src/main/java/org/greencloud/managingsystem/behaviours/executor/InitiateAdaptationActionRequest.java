package org.greencloud.managingsystem.behaviours.executor;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.time.TimeConverter.convertMillisecondsToTimeString;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.types.GoalType;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiates the adaptation action request for the given agent and handles its execution result.
 */
public class InitiateAdaptationActionRequest extends AchieveREInitiator {

	private static final LogBuilder log = constructLogBuilder(InitiateAdaptationActionRequest.class);

	private final ManagingAgent myManagingAgent;
	private final ManagingAgentNode managingAgentNode;
	private final AdaptationAction adaptationAction;
	private final AID targetAgent;
	private final Map<GoalType, Double> initialGoalQualities;
	private final Runnable postActionHandler;
	private final Runnable enablePlanAction;
	private final Timer timer;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent                managing agent that executes the behaviour
	 * @param message              request of adaptation that is to be sent to the targeted agent
	 * @param initialGoalQualities values of the goal qualities before performing the adaptation
	 * @param postActionHandler    action performed after adaptation is finished
	 * @param enablePlanAction     method performed in order to enable availability of an action corresponding to
	 *                             given plan
	 * @param adaptationAction     action that is to be executed
	 */
	public InitiateAdaptationActionRequest(final ManagingAgent agent,
			final ACLMessage message,
			final Map<GoalType, Double> initialGoalQualities,
			final Runnable postActionHandler,
			final Runnable enablePlanAction,
			final AdaptationAction adaptationAction) {
		super(agent, message);
		this.timer = new Timer();
		timer.startTimeMeasure(getCurrentTime());

		this.targetAgent = (AID) message.getAllReceiver().next();
		this.myManagingAgent = agent;
		this.managingAgentNode = myManagingAgent.getAgentNode();
		this.initialGoalQualities = initialGoalQualities;
		this.postActionHandler = postActionHandler;
		this.enablePlanAction = enablePlanAction;
		this.adaptationAction = adaptationAction;
	}

	/**
	 * Method handles INFORM message coming from given adapted agent which tells that the
	 * adaptation was successfully executed.
	 * Method schedules the {@link VerifyAdaptationActionResult} behaviour that will be responsible for
	 * verifying the adaptation outcome.
	 *
	 * @param inform message received from the target agent
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final Instant finishTime = getCurrentTime();
		final long executionDuration = timer.stopTimeMeasure(finishTime);
		final String formattedDuration = convertMillisecondsToTimeString(executionDuration);
		final AdaptationActionTypeEnum actionType = adaptationAction.getAction();

		log.messageInfo("Adaptation action id {} on agent {} at {} was executed successfully during {}!",
				actionType, targetAgent, finishTime, formattedDuration);

		executePostActionHandlers();
		scheduleVerificationBehaviours(finishTime, executionDuration);
		managingAgentNode.logNewAdaptation(actionType, finishTime, Optional.of(targetAgent.getLocalName()));
	}

	/**
	 * Method handles FAILURE message coming from given agent selected for adaptation which tells that the
	 * adaptation execution has failed.
	 *
	 * @param failure failure message received from the target agent
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		log.messageInfo("Adaptation action id {} on agent {} execution failed!", adaptationAction.getAction(),
				targetAgent);
		myManagingAgent.getAgentNode()
				.getDatabaseClient().setAdaptationActionAvailability(adaptationAction.getActionId(), true);
	}

	private void executePostActionHandlers() {
		ofNullable(postActionHandler).ifPresent(Runnable::run);
	}

	private void scheduleVerificationBehaviours(final Instant finishTime, final long duration) {
		myManagingAgent.addBehaviour(new VerifyAdaptationActionResult(myManagingAgent,
				finishTime, duration, adaptationAction, targetAgent, initialGoalQualities, enablePlanAction));
	}
}
