package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.CONNECT_GREEN_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.GREEN_SOURCE_ALREADY_CONNECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.GREEN_SOURCE_CONNECTION_TEMPLATE;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static java.util.Collections.singletonList;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviours listens for messages from Green Sources informing about additional connection
 */
public class ListenForAdditionalGreenSourceService extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForAdditionalGreenSourceService.class);

	private final ServerAgent myServerAgent;

	/**
	 * Default constructor
	 *
	 * @param myServerAgent agent executing behaviour
	 */
	public ListenForAdditionalGreenSourceService(ServerAgent myServerAgent) {
		super(myServerAgent);
		this.myServerAgent = myServerAgent;
	}

	/**
	 * Method listens for the message coming from the Green Source informing that it will be temporarily
	 * connected to the Server according to the received adaptation plan.
	 */
	@Override
	public void action() {
		final ACLMessage request = myServerAgent.receive(GREEN_SOURCE_CONNECTION_TEMPLATE);

		if (Objects.nonNull(request)) {
			if (myServerAgent.getOwnedGreenSources().containsKey(request.getSender())) {
				logger.info(GREEN_SOURCE_ALREADY_CONNECTED_LOG, request.getSender().getName());
				myServerAgent.send(prepareRefuseReply(request.createReply()));
			} else {
				logger.info(CONNECT_GREEN_SOURCE_LOG, request.getSender().getName());
				myServerAgent.manageConfig().connectNewGreenSourcesToServer(singletonList(request.getSender()));
				myServerAgent.send(prepareInformReply(request.createReply()));
			}
		} else {
			block();
		}
	}
}