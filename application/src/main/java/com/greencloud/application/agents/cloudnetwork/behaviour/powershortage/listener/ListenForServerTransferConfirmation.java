package com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener.logs.PowerShortageCloudListenerLog.SERVER_TRANSFER_CONFIRMED_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.listener.templates.PowerShortageCloudMessageTemplates.SERVER_JOB_TRANSFER_CONFIRMATION_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchContent;
import static jade.lang.acl.MessageTemplate.and;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.handler.HandleJobTransferToServer;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JsonMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/**
 * Behaviour receives the messages confirming that the job transfer was accepted in given server
 */
public class ListenForServerTransferConfirmation extends MsgReceiver {

	private static final int EXPIRATION_TIME = 2000;
	private static final Logger logger = LoggerFactory.getLogger(ListenForServerTransferConfirmation.class);

	private final CloudNetworkAgent myCloudNetworkAgent;
	private final ACLMessage replyMessage;
	private final PowerShortageJob powerShortageJob;
	private final AID server;

	private ListenForServerTransferConfirmation(final Agent agent, final MessageTemplate template,
			final DataStore dataStore, final ACLMessage replyMessage, final PowerShortageJob powerShortageJob,
			final AID server) {
		super(agent, template, EXPIRATION_TIME, dataStore, null);
		this.replyMessage = replyMessage;
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.powerShortageJob = powerShortageJob;
		this.server = server;
	}

	/**
	 * Method creates the behaviour.
	 *
	 * @param agent            agent executing the behaviour
	 * @param replyMessage     reply message sent to Server informing about the transfer status
	 * @param powerShortageJob job that is being transferred
	 * @param server           server to which the job is transferred
	 * @param dataStore        data store
	 */
	public static Behaviour createFor(final Agent agent, final ACLMessage replyMessage,
			final PowerShortageJob powerShortageJob, final AID server, final DataStore dataStore) {
		final MessageTemplate template = and(SERVER_JOB_TRANSFER_CONFIRMATION_TEMPLATE,
				MatchContent(getExpectedContent(powerShortageJob)));
		return new ListenForServerTransferConfirmation(agent, template, dataStore, replyMessage, powerShortageJob,
				server);
	}

	private static String getExpectedContent(final PowerShortageJob powerShortageJob) {
		try {
			return JsonMapper.getMapper().writeValueAsString(mapToJobInstanceId(powerShortageJob));
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * Method listens for the messages coming from the Server confirming that the job transfer has been accepted by the Server
	 */
	@Override
	protected void handleMessage(ACLMessage msg) {
		if (Objects.nonNull(msg)) {
			MDC.put(MDC_JOB_ID, powerShortageJob.getJobInstanceId().getJobId());
			logger.info(SERVER_TRANSFER_CONFIRMED_LOG, powerShortageJob.getJobInstanceId().getJobId());

			final ACLMessage replyToServerRequest = prepareReply(replyMessage, TRANSFER_SUCCESSFUL_MESSAGE, INFORM);

			displayMessageArrow(myCloudNetworkAgent, replyMessage.getAllReceiver());

			myCloudNetworkAgent.send(replyToServerRequest);
			myCloudNetworkAgent.addBehaviour(
					HandleJobTransferToServer.createFor(myCloudNetworkAgent, powerShortageJob, server));
			myAgent.removeBehaviour(this);
		}
	}

}
