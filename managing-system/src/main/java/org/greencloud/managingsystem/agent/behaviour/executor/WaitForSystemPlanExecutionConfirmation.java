package org.greencloud.managingsystem.agent.behaviour.executor;


import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONFIRM_SYSTEM_PLAN_MESSAGE;
import static org.greencloud.commons.utils.time.TimeConverter.convertMillisecondsToTimeString;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult.createForSystemAction;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.ACTION_SYSTEM_FAILED_LOG;
import static org.greencloud.managingsystem.agent.behaviour.executor.logs.ManagingExecutorLog.COMPLETED_SYSTEM_ACTION_LOG;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.SYSTEM_ADAPTATION_PLAN_CONFIRMATION_TIMEOUT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.planner.plans.SystemPlan;
import org.slf4j.Logger;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.goal.GoalEnum;
import org.greencloud.commons.domain.timer.Timer;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/**
 * Behaviour is responsible for waiting for the message from main system plan agent, that will confirm that
 * a system plan has been conducted successfully
 */
public class WaitForSystemPlanExecutionConfirmation extends MsgReceiver {
	private static final Logger logger = getLogger(WaitForSystemPlanExecutionConfirmation.class);

	private final ManagingAgent myManagingAgent;
	private final Timer actionExecutionTimer;
	private final SystemPlan plan;
	private final Map<GoalEnum, Double> initialGoalQualities;
	private final AdaptationAction actionToBeExecuted;

	private WaitForSystemPlanExecutionConfirmation(final ManagingAgent agent, final MessageTemplate template,
			final SystemPlan systemAdaptationPlan, final Map<GoalEnum, Double> initialGoalQualities,
			final AdaptationAction actionToBeExecuted) {
		super(agent, template, SYSTEM_ADAPTATION_PLAN_CONFIRMATION_TIMEOUT + currentTimeMillis(), null, null);

		this.myManagingAgent = agent;
		this.actionExecutionTimer = new Timer();
		actionExecutionTimer.startTimeMeasure(getCurrentTime());
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
	public static WaitForSystemPlanExecutionConfirmation create(final ManagingAgent agent, final String agentName,
			final String containerName, final SystemPlan systemAdaptationPlan,
			final Map<GoalEnum, Double> initialGoalQualities, final AdaptationAction actionToBeExecuted) {
		final String protocol = String.join("_", CONFIRM_SYSTEM_PLAN_MESSAGE, agentName, containerName);
		final MessageTemplate template = and(MatchPerformative(INFORM), MatchProtocol(protocol));
		return new WaitForSystemPlanExecutionConfirmation(agent, template, systemAdaptationPlan,
				initialGoalQualities, actionToBeExecuted);
	}

	/**
	 * Method listens for the messages coming from an agent which confirms successful execution of the system plan
	 */
	@Override
	protected void handleMessage(ACLMessage msg) {
		if (nonNull(msg)) {
			var actionExecutionTime = getCurrentTime();
			final long executionDuration = actionExecutionTimer.stopTimeMeasure(actionExecutionTime);
			final String formattedDuration = convertMillisecondsToTimeString(executionDuration);
			logger.info(COMPLETED_SYSTEM_ACTION_LOG, plan.getAdaptationActionEnum(), actionExecutionTime,
					formattedDuration);

			myManagingAgent.addBehaviour(createForSystemAction(myManagingAgent, actionToBeExecuted, initialGoalQualities,
					plan.enablePlanAction(), executionDuration));

			myManagingAgent.getAgentNode().logNewAdaptation(actionToBeExecuted.getAction(),
					getCurrentTime(), empty());
		} else {
			logger.info(ACTION_SYSTEM_FAILED_LOG, plan.getAdaptationActionEnum());
			myManagingAgent.getAgentNode().getDatabaseClient()
					.setAdaptationActionAvailability(actionToBeExecuted.getActionId(), true);
		}
	}

}
