package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.RECEIVED_POWER_INFORMATION_REQUEST_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.CLOUD_NETWORK_INFORMATION_TEMPLATE;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for requests sent by the Cloud Network asking for a specific details regarding provided service
 */
public class ListenForCloudNetworkInformationRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForCloudNetworkInformationRequest.class);

	private final ServerAgent myServerAgent;

	/**
	 * Default constructor
	 *
	 * @param myServerAgent agent executing behaviour
	 */
	public ListenForCloudNetworkInformationRequest(ServerAgent myServerAgent) {
		super(myServerAgent);
		this.myServerAgent = myServerAgent;
	}

	/**
	 * Method listens for the message coming from the Cloud Network asking about Server service details
	 */
	@Override
	public void action() {
		final ACLMessage request = myServerAgent.receive(CLOUD_NETWORK_INFORMATION_TEMPLATE);

		if (Objects.nonNull(request)) {
			logger.info(RECEIVED_POWER_INFORMATION_REQUEST_LOG, request.getSender().getLocalName());
			myServerAgent.send(prepareStringReply(request.createReply(),
					valueOf(myServerAgent.getInitialMaximumCapacity()), INFORM));
		} else {
			block();
		}
	}
}
