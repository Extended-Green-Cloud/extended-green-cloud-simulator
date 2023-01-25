package com.greencloud.application.agents.server.behaviour.df.listener;

import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.CONNECT_GREEN_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.DEACTIVATE_GREEN_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.DISCONNECT_GREEN_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.GREEN_SOURCE_ALREADY_CONNECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.GREEN_SOURCE_NOT_CONNECTED_TO_SERVER_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.logs.ServerDFListenerLog.GREEN_SOURCE_NOT_DEACTIVATED_LOG;
import static com.greencloud.application.agents.server.behaviour.df.listener.templates.DFServerMessageTemplates.GREEN_SOURCE_UPDATE_TEMPLATE;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONNECT_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DEACTIVATE_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DISCONNECT_GREEN_SOURCE_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareInformReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.server.ServerAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for messages related to update in given Green Source service
 */
public class ListenForGreenSourceServiceUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForGreenSourceServiceUpdate.class);

	private final ServerAgent myServerAgent;

	/**
	 * Default constructor
	 *
	 * @param myServerAgent agent executing behaviour
	 */
	public ListenForGreenSourceServiceUpdate(ServerAgent myServerAgent) {
		super(myServerAgent);
		this.myServerAgent = myServerAgent;
	}

	/**
	 * Method listens for the message coming from the Green Source informing about its service status update.
	 * 1. In case of Green Source DEACTIVATION - server turns of a Green Source service so that it will not be taken
	 * under consideration for future job execution
	 * 2. In case of Green Source DISCONNECTION - server removes a given Green Source from its list. It is important to
	 * note that Server can remove only Green Sources which were deactivated
	 * 3. In case of Green Source CONNECTION - server adds a given Green Source to its list.
	 */
	@Override
	public void action() {
		final ACLMessage request = myServerAgent.receive(GREEN_SOURCE_UPDATE_TEMPLATE);

		if (Objects.nonNull(request)) {
			switch (request.getProtocol()) {
				case DEACTIVATE_GREEN_SOURCE_PROTOCOL -> handleGreenSourceDeactivation(request);
				case DISCONNECT_GREEN_SOURCE_PROTOCOL -> handleGreenSourceDisconnection(request);
				case CONNECT_GREEN_SOURCE_PROTOCOL -> handleGreenSourceConnection(request);
			}
		} else {
			block();
		}
	}

	private void handleGreenSourceDeactivation(final ACLMessage request) {
		final AID greenSource = request.getSender();

		if (!myServerAgent.getOwnedGreenSources().containsKey(greenSource)) {
			logger.info(GREEN_SOURCE_NOT_CONNECTED_TO_SERVER_LOG, greenSource.getName());
			myServerAgent.send(prepareRefuseReply(request.createReply()));
		} else {
			logger.info(DEACTIVATE_GREEN_SOURCE_LOG, greenSource.getName());
			myServerAgent.getOwnedGreenSources().replace(greenSource, false);
			myServerAgent.send(prepareInformReply(request.createReply()));
		}
	}

	private void handleGreenSourceDisconnection(final ACLMessage request) {
		final AID greenSource = request.getSender();

		if (!myServerAgent.getOwnedGreenSources().containsKey(greenSource)) {
			logger.info(GREEN_SOURCE_NOT_CONNECTED_TO_SERVER_LOG, greenSource.getName());
			myServerAgent.send(prepareRefuseReply(request.createReply()));
		} else if (TRUE.equals(myServerAgent.getOwnedGreenSources().get(greenSource)) ||
				isGreenSourceExecutingJobs(greenSource)) {
			logger.info(GREEN_SOURCE_NOT_DEACTIVATED_LOG, greenSource.getName());
			myServerAgent.send(prepareRefuseReply(request.createReply()));
		} else {
			logger.info(DISCONNECT_GREEN_SOURCE_LOG, greenSource.getName());
			myServerAgent.getOwnedGreenSources().remove(greenSource);
			myServerAgent.send(prepareInformReply(request.createReply()));
		}
	}

	private void handleGreenSourceConnection(final ACLMessage request) {
		if (myServerAgent.getOwnedGreenSources().containsKey(request.getSender())) {
			logger.info(GREEN_SOURCE_ALREADY_CONNECTED_LOG, request.getSender().getName());
			myServerAgent.send(prepareRefuseReply(request.createReply()));
		} else {
			logger.info(CONNECT_GREEN_SOURCE_LOG, request.getSender().getName());
			myServerAgent.manageConfig().connectNewGreenSourcesToServer(singletonList(request.getSender()));
			myServerAgent.send(prepareInformReply(request.createReply()));
		}
	}

	private boolean isGreenSourceExecutingJobs(final AID greenSource) {
		return myServerAgent.getGreenSourceForJobMap().values().stream()
				.anyMatch(executor -> executor.equals(greenSource));
	}

}
