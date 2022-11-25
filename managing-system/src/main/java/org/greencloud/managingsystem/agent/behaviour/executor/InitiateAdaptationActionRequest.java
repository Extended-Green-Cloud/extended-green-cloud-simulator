package org.greencloud.managingsystem.agent.behaviour.executor;

import static java.time.Instant.now;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.ACTION_FAILED_LOG;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.COMPLETED_ACTION_LOG;

import java.time.Instant;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Initiates the adaptation action request for the given agent
 */
public class InitiateAdaptationActionRequest extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateAdaptationActionRequest.class);

	private final String adaptationActionId;
	private final AID targetAgent;
	private final Double initialGoalQuality;

	public InitiateAdaptationActionRequest(Agent agent, ACLMessage message, Double initialGoalQuality) {
		super(agent, message);
		this.adaptationActionId = message.getConversationId();
		this.targetAgent = (AID) message.getAllReceiver().next();
		this.initialGoalQuality = initialGoalQuality;
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(COMPLETED_ACTION_LOG, adaptationActionId, targetAgent);
		Instant actionTimestamp = now();
		Instant verifyTime = actionTimestamp.plusSeconds(VERIFY_ADAPTATION_ACTION_DELAY_IN_SECONDS);
		var verifyingBehaviour = new VerifyAdaptationActionResult(myAgent, actionTimestamp, verifyTime,
				adaptationActionId, targetAgent, initialGoalQuality);
		myAgent.addBehaviour(verifyingBehaviour);
		myAgent.removeBehaviour(this);
	}

	/**
	 * If the action fails it should be released immediately
	 *
	 * @param failure failure message received from the target agent
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		logger.info(ACTION_FAILED_LOG, adaptationActionId, targetAgent);
		((ManagingAgent) myAgent).getAgentNode().getDatabaseClient()
				.setAdaptationActionAvailability(Integer.parseInt(adaptationActionId), true);
		myAgent.removeBehaviour(this);
	}
}
