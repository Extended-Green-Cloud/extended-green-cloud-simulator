package com.greencloud.application.agents.greenenergy.behaviour.adaptation;

import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.DISCONNECTION_FAILED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.adaptation.logs.AdaptationSourceLog.DISCONNECTION_SUCCEEDED_LOG;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;
import static jade.lang.acl.ACLMessage.REQUEST;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.message.MessageBuilder;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour requests Green Source disconnection in a given server agent
 */
public class InitiateGreenSourceDisconnection extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateGreenSourceDisconnection.class);

	private final GreenEnergyAgent myGreenAgent;
	private final ACLMessage adaptationMessage;

	private InitiateGreenSourceDisconnection(GreenEnergyAgent agent, ACLMessage deactivationMessage) {
		super(agent, deactivationMessage);
		this.myGreenAgent = agent;
		this.adaptationMessage = agent.adapt().getGreenSourceDisconnectionState().getOriginalAdaptationMessage();
	}

	/**
	 * Method creating a behaviour
	 *
	 * @param greenEnergyAgent green source executing the behaviour
	 * @param server           server to which the message is sent
	 * @return new InitiateGreenSourceDeactivation behaviour
	 */
	public static InitiateGreenSourceDisconnection create(final GreenEnergyAgent greenEnergyAgent, final AID server) {
		final ACLMessage deactivationMessage = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withMessageProtocol(DISCONNECT_GREEN_SOURCE_PROTOCOL)
				.withStringContent(DISCONNECT_GREEN_SOURCE_PROTOCOL)
				.withReceivers(server)
				.build();
		return new InitiateGreenSourceDisconnection(greenEnergyAgent, deactivationMessage);
	}

	/**
	 * Method handles the REFUSE response retrieved from Server informing that the Green Source disconnection
	 * couldn't be completed.
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		logger.info(DISCONNECTION_FAILED_LOG, refuse.getSender().getName());
		myGreenAgent.adapt().getGreenSourceDisconnectionState().reset();
		myGreenAgent.send(prepareFailureReply(adaptationMessage.createReply()));
	}

	/**
	 * Method handles the INFORM response retrieved from Server informing that the Green Source was successfully
	 * disconnected
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		logger.info(DISCONNECTION_SUCCEEDED_LOG, inform.getSender().getName());
		myGreenAgent.adapt().getGreenSourceDisconnectionState().reset();
		myGreenAgent.send(prepareInformReply(adaptationMessage.createReply()));

		if (Objects.nonNull(myGreenAgent.getAgentNode())) {
			((GreenEnergyAgentNode) myGreenAgent.getAgentNode()).updateServerConnection(
					inform.getSender().getName().split("@")[0], false);
		}
	}
}
