package org.greencloud.managingsystem.agent.behaviour.executor;

import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.ACTION_FAILED_LOG;
import static org.greencloud.managingsystem.service.executor.logs.ExecutorLogs.COMPLETED_ACTION_LOG;

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

	public InitiateAdaptationActionRequest(Agent a, ACLMessage msg) {
		super(a, msg);
		this.adaptationActionId = msg.getConversationId();
		this.targetAgent = (AID) msg.getAllReceiver().next();
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(COMPLETED_ACTION_LOG, adaptationActionId, targetAgent);
		myAgent.removeBehaviour(this);
	}

	@Override
	protected void handleFailure(ACLMessage failure) {
		logger.info(ACTION_FAILED_LOG, adaptationActionId, targetAgent);
		myAgent.removeBehaviour(this);
	}
}
