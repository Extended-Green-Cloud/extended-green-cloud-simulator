package org.greencloud.managingsystem.behaviours.executor;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.String.join;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONFIRM_SYSTEM_PLAN_MESSAGE;
import static org.greencloud.commons.utils.time.TimeConverter.convertMillisecondsToTimeString;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.managingsystem.agents.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.types.GoalType;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/**
 * Behaviour waits for the message from the main system plan agent, that will confirm whether
 * a system plan has been conducted successfully.
 */
public class WaitForSystemPlanExecutionConfirmation extends MsgReceiver {

	private static final LogBuilder log = constructLogBuilder(WaitForSystemPlanExecutionConfirmation.class);

	private final ManagingAgent myManagingAgent;
	private final Timer actionExecutionTimer;
	private final SystemPlan plan;
	private final Map<GoalType, Double> initialGoalQualities;
	private final AdaptationAction actionToBeExecuted;

	private WaitForSystemPlanExecutionConfirmation(final ManagingAgent agent,
			final MessageTemplate template,
			final SystemPlan systemAdaptationPlan,
			final Map<GoalType, Double> initialGoalQualities,
			final AdaptationAction actionToBeExecuted) {
		super(agent, template,
				agent.getProperties().getConfig().getSystemAdaptationPlanConfirmationTimeout()
						+ getCurrentTime().toEpochMilli(),
				null, null);

		this.actionExecutionTimer = new Timer();
		actionExecutionTimer.startTimeMeasure(getCurrentTime());

		this.myManagingAgent = agent;
		this.plan = systemAdaptationPlan;
		this.initialGoalQualities = initialGoalQualities;
		this.actionToBeExecuted = actionToBeExecuted;
	}

	/**
	 * Method creates the behaviour.
	 *
	 * @param agent                agent executing the behaviour
	 * @param agentName            name of the agent from which the confirmation is expected
	 * @param containerName        name of the container in which confirming agent reside
	 * @param initialGoalQualities goal qualities used later in adaptation action verification
	 * @param actionToBeExecuted   information about action that is being executed
	 */
	public static WaitForSystemPlanExecutionConfirmation create(final ManagingAgent agent,
			final String agentName,
			final String containerName,
			final SystemPlan systemAdaptationPlan,
			final Map<GoalType, Double> initialGoalQualities,
			final AdaptationAction actionToBeExecuted) {
		final String protocol = join("_", CONFIRM_SYSTEM_PLAN_MESSAGE, agentName, containerName);
		final MessageTemplate template = and(MatchPerformative(INFORM), MatchProtocol(protocol));

		return new WaitForSystemPlanExecutionConfirmation(agent, template, systemAdaptationPlan,
				initialGoalQualities, actionToBeExecuted);
	}

	/**
	 * Method listens for the messages coming from an agent which confirms successful execution of the system plan
	 */
	@Override
	protected void handleMessage(final ACLMessage msg) {
		ofNullable(msg).ifPresentOrElse(_ -> handleSuccessfulPlanExecution(), this::handlePlanExecutionFailure);
	}

	private void handleSuccessfulPlanExecution() {
		final Instant finishTime = getCurrentTime();
		final long executionDuration = actionExecutionTimer.stopTimeMeasure(finishTime);

		log.messageInfo("Adaptation system action id {} at {} was executed successfully during {}!",
				plan.getAdaptationActionEnum(), finishTime, convertMillisecondsToTimeString(executionDuration));

		myManagingAgent.addBehaviour(new VerifyAdaptationActionResult(myManagingAgent, finishTime, executionDuration,
				actionToBeExecuted, initialGoalQualities, plan.enablePlanAction()));
		myManagingAgent.getAgentNode().logNewAdaptation(actionToBeExecuted.getAction(), getCurrentTime(), empty());
	}

	private void handlePlanExecutionFailure() {
		log.messageInfo("Adaptation system action id {} execution failed!", plan.getAdaptationActionEnum());
		myManagingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(actionToBeExecuted.getActionId(), true);
	}

}
