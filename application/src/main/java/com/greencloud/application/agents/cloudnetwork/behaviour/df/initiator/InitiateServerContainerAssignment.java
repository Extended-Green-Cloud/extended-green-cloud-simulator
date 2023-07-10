package com.greencloud.application.agents.cloudnetwork.behaviour.df.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.df.initiator.logs.CloudNetworkDFInitiatorLog.ASSIGN_CONTAINER_FOR_SERVER;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ASK_FOR_CONTAINER_PROTOCOL;
import static com.greencloud.application.utils.MessagingUtils.retrieveForPerformative;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour requests the information about Server container assignment from newly added server
 */
public class InitiateServerContainerAssignment extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateServerContainerAssignment.class);

	private final CloudNetworkAgent myCloudNetwork;

	private InitiateServerContainerAssignment(final Agent agent, final ACLMessage msg) {
		super(agent, msg);
		this.myCloudNetwork = (CloudNetworkAgent) agent;
	}

	/**
	 * Method creates InitiateServerContainerAssignment behaviour
	 *
	 * @param cloudNetworkAgent agent executing the behaviour
	 * @param serversToAsk      servers that are to be asked for their assigned containers
	 * @return InitiateServerContainerAssignment
	 */
	public static InitiateServerContainerAssignment create(final CloudNetworkAgent cloudNetworkAgent,
			final Set<AID> serversToAsk) {
		final ACLMessage message = MessageBuilder.builder()
				.withMessageProtocol(ASK_FOR_CONTAINER_PROTOCOL)
				.withStringContent(ASK_FOR_CONTAINER_PROTOCOL)
				.withPerformative(REQUEST)
				.withReceivers(serversToAsk)
				.build();
		return new InitiateServerContainerAssignment(cloudNetworkAgent, message);
	}

	/**
	 * Method handles the messages containing the information regarding servers container allocation
	 *
	 * @param resultNotifications vector of retrieved results
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void handleAllResultNotifications(final Vector resultNotifications) {
		final Collection<ACLMessage> informs = retrieveForPerformative((Vector<ACLMessage>) resultNotifications,
				INFORM);

		informs.forEach(inform -> {
			logger.info(ASSIGN_CONTAINER_FOR_SERVER, inform.getSender().getLocalName());
			myCloudNetwork.getServerContainers().put(inform.getContent(), inform.getSender());
		});
	}
}
