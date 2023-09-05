package com.greencloud.application.agents.server.behaviour.adaptation.initiator;

import static com.greencloud.application.agents.server.behaviour.adaptation.initiator.logs.AdaptationServerLog.ENABLING_FAILED_LOG;
import static com.greencloud.application.agents.server.behaviour.adaptation.initiator.logs.AdaptationServerLog.ENABLING_SUCCEEDED_LOG;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareInformReply;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.commons.message.MessageBuilder;
import com.gui.agents.ServerAgentNode;

import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour requests in the parent CNA that the server should be enabled
 */
public class InitiateServerEnabling extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateServerEnabling.class);

	private final ServerAgent myServerAgent;
	private final ACLMessage adaptationMessage;

	private InitiateServerEnabling(final ServerAgent agent, final ACLMessage disablingMessage,
			final ACLMessage adaptationMessage) {
		super(agent, disablingMessage);

		this.myServerAgent = agent;
		this.adaptationMessage = adaptationMessage;
	}

	/**
	 * Method creating a behaviour
	 *
	 * @param myServerAgent     server executing the behaviour
	 * @param adaptationMessage original adaptation request
	 * @return new InitiateServerEnabling behaviour
	 */
	public static InitiateServerEnabling create(final ServerAgent myServerAgent, final ACLMessage adaptationMessage) {
		final ACLMessage disablingMessage = MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withStringContent(ENABLE_SERVER_PROTOCOL)
				.withReceivers(myServerAgent.getOwnerCloudNetworkAgent())
				.build();
		return new InitiateServerEnabling(myServerAgent, disablingMessage, adaptationMessage);
	}

	/**
	 * Method handles the INFORM response retrieved from CNA informing that the Server was successfully
	 * enabled
	 *
	 * @param inform retrieved response
	 */
	@Override
	protected void handleInform(final ACLMessage inform) {
		logger.info(ENABLING_SUCCEEDED_LOG, inform.getSender().getName());
		myServerAgent.send(prepareInformReply(adaptationMessage));

		final ACLMessage confirmationMessage = MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(ENABLE_SERVER_PROTOCOL)
				.withReceivers(myServerAgent.getOwnerCloudNetworkAgent())
				.build();

		myServerAgent.send(confirmationMessage);
		((ServerAgentNode) myServerAgent.getAgentNode()).enableServer();
	}

	/**
	 * Method handles the REFUSE response retrieved from CNA informing that the Server does not exist in a Cloud
	 * Network
	 *
	 * @param refuse retrieved response
	 */
	@Override
	protected void handleRefuse(final ACLMessage refuse) {
		logger.info(ENABLING_FAILED_LOG, refuse.getSender().getName());
		myServerAgent.disable();
		myServerAgent.manage().writeStateToDatabase();
		myServerAgent.send(prepareFailureReply(adaptationMessage));
	}

}
