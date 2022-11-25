package org.greencloud.managingsystem.agent.behaviour.executor;

import static java.time.Instant.now;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.ACTION_FAILED_LOG;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.COMPLETED_ACTION_LOG;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiates the adaptation action request for the given agent and handles its execution result, either
 * successful or failed execution.
 */
public class InitiateAdaptationActionRequest extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateAdaptationActionRequest.class);

	private final ManagingAgent myManagingAgent;
	private final String adaptationActionId;
	private final AID targetAgent;
	private final Double initialGoalQuality;

	public InitiateAdaptationActionRequest(Agent agent, ACLMessage message, Double initialGoalQuality) {
		super(agent, message);
		this.adaptationActionId = message.getConversationId();
		this.targetAgent = (AID) message.getAllReceiver().next();
		this.initialGoalQuality = initialGoalQuality;
		this.myManagingAgent = (ManagingAgent) agent;
	}

	/**
	 * If the action is correctly executes Agent responds with an INFORM message. In that case
	 * a verification of the executed action must be scheduled. The {@link VerifyAdaptationActionResult}
	 * is scheduled after the period defined in VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS constant.
	 *
	 * @param inform message received from the target agent
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(COMPLETED_ACTION_LOG, adaptationActionId, targetAgent);
		scheduleVerifyBehaviour();
		myManagingAgent.removeBehaviour(this);
	}

	private void scheduleVerifyBehaviour() {
		var verifyingBehaviour = new VerifyAdaptationActionResult(myManagingAgent, now(), adaptationActionId,
				targetAgent, initialGoalQuality);
		myManagingAgent.addBehaviour(verifyingBehaviour);
	}

	/**
	 * If the action fails it should be released immediately
	 *
	 * @param failure failure message received from the target agent
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		logger.info(ACTION_FAILED_LOG, adaptationActionId, targetAgent);
		myManagingAgent.getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(Integer.parseInt(adaptationActionId), true);
		myManagingAgent.removeBehaviour(this);
	}
}
