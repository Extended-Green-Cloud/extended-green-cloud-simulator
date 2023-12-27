package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.RECEIVED_CONTAINER_INFORMATION_REQUEST_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.CLOUD_NETWORK_CONTAINER_TEMPLATE;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the Cloud Network request for the information about server's container allocation
 */
public class ListenForCloudNetworkContainerRequest extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForCloudNetworkContainerRequest.class);

	private final ServerAgent myServerAgent;

	/**
	 * Default constructor
	 *
	 * @param myServerAgent agent executing behaviour
	 */
	public ListenForCloudNetworkContainerRequest(ServerAgent myServerAgent) {
		super(myServerAgent);
		this.myServerAgent = myServerAgent;
	}

	/**
	 * Method listens for the message coming from the Cloud Network asking about Server container allocation
	 */
	@Override
	public void action() {
		final ACLMessage request = myServerAgent.receive(CLOUD_NETWORK_CONTAINER_TEMPLATE);

		if (nonNull(request)) {
			logger.info(RECEIVED_CONTAINER_INFORMATION_REQUEST_LOG, request.getSender().getLocalName());
			myServerAgent.send(prepareStringReply(request, myServerAgent.getAllocatedContainer(), INFORM));
		} else {
			block();
		}
	}
}
