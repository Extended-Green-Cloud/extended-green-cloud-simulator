package com.greencloud.application.agents.cloudnetwork.behaviour.df.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.df.listener.logs.CloudNetworkDFListenerLog.SERVER_FOR_STATUS_CHANGE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.listener.logs.CloudNetworkDFListenerLog.SERVER_STATUS_CHANGE_IN_CNA_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.df.listener.templates.DFCloudNetworkMessageTemplates.SERVER_STATUS_CHANGE_TEMPLATE;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareInformReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;

import org.slf4j.Logger;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the messages informing about disabling given Server
 */
public class ListenForServerStatusChange extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForServerStatusChange.class);

	private final CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Default constructor
	 *
	 * @param myCloudNetworkAgent agent executing behaviour
	 */
	public ListenForServerStatusChange(CloudNetworkAgent myCloudNetworkAgent) {
		super(myCloudNetworkAgent);
		this.myCloudNetworkAgent = myCloudNetworkAgent;
	}

	/**
	 * Method listens for the message coming from the Servers informing that a given server is going to be disabled
	 */
	@Override
	public void action() {
		final ACLMessage msg = myCloudNetworkAgent.receive(SERVER_STATUS_CHANGE_TEMPLATE);

		if (Objects.nonNull(msg)) {
			handleServerStatusChangeRequest(msg);
		} else {
			block();
		}
	}

	private void handleServerStatusChangeRequest(final ACLMessage request) {
		final AID serverWithStatusChange = request.getSender();
		final String action = request.getProtocol().equals(DISABLE_SERVER_PROTOCOL) ? "disabling" : "enabling";
		final boolean newStatus = request.getProtocol().equals(ENABLE_SERVER_PROTOCOL);

		if (myCloudNetworkAgent.getOwnedServers().containsKey(serverWithStatusChange)) {
			logger.info(SERVER_STATUS_CHANGE_IN_CNA_LOG, action, serverWithStatusChange.getLocalName());
			myCloudNetworkAgent.getOwnedServers().replace(serverWithStatusChange, newStatus);
			myCloudNetworkAgent.send(prepareInformReply(request));
		} else {
			logger.info(SERVER_FOR_STATUS_CHANGE_NOT_FOUND_LOG, action, serverWithStatusChange.getLocalName());
			myCloudNetworkAgent.send(prepareRefuseReply(request));
		}
	}

}
