package com.greencloud.application.agents.greenenergy.behaviour.adaptation;

import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.CONNECTION_FAILED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.CONNECTION_SUCCEEDED_LOG;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.gui.agents.GreenEnergyAgentNode;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour initiates connection with given server
 */
public class InitiateNewServerConnection extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewServerConnection.class);

	private final GreenEnergyAgent myGreenAgent;
	private final ACLMessage adaptationMessage;
	private final String serverToBeConnected;

	/**
	 * Behaviours constructor
	 *
	 * @param agent             green source executing the behaviour
	 * @param connectionMessage message requesting connection in the given server
	 * @param adaptationMessage original adaptation request
	 */
	public InitiateNewServerConnection(GreenEnergyAgent agent, ACLMessage connectionMessage,
			ACLMessage adaptationMessage, String serverToBeConnected) {
		super(agent, connectionMessage);
		this.myGreenAgent = agent;
		this.adaptationMessage = adaptationMessage;
		this.serverToBeConnected = serverToBeConnected;
	}

	/**
	 * Method handles the REFUSE response retrieved from Server informing that the Green Source is already connected
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		logger.info(CONNECTION_FAILED_LOG, refuse.getSender().getName());
		myGreenAgent.send(prepareFailureReply(adaptationMessage.createReply()));
	}

	/**
	 * Method handles the INFORM response retrieved from Server informing that the connection was successful
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(CONNECTION_SUCCEEDED_LOG, inform.getSender().getName());
		myGreenAgent.send(prepareInformReply(adaptationMessage.createReply()));

		if (Objects.nonNull(myGreenAgent.getAgentNode())) {
			((GreenEnergyAgentNode) myGreenAgent.getAgentNode()).updateServerConnection(
					serverToBeConnected, true);
		}
	}

}