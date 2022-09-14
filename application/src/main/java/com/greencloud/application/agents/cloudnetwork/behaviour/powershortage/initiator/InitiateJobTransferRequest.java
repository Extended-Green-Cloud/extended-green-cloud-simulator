package com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.handler.HandleJobTransferToServer.createFor;
import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.initiator.logs.PowerShortageCloudInitiatorLog.SERVER_TRANSFER_CHOSEN_SERVER_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.initiator.logs.PowerShortageCloudInitiatorLog.SERVER_TRANSFER_NO_RESPONSE_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.powershortage.initiator.logs.PowerShortageCloudInitiatorLog.SERVER_TRANSFER_NO_SERVERS_AVAILABLE_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;
import static com.greencloud.application.messages.MessagingUtils.retrieveProposals;
import static com.greencloud.application.messages.MessagingUtils.retrieveValidMessages;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.ServerData;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JsonMapper;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviours sends the CFP to remaining servers looking for job transfer and selects the one which will
 * handle the remaining job execution
 */
public class InitiateJobTransferRequest extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobTransferRequest.class);

	private final CloudNetworkAgent myCloudNetworkAgent;
	private final ACLMessage serverRequest;
	private final PowerShortageJob jobTransfer;
	private final AID jobClient;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent         agent which is executing the behaviour
	 * @param cfp           call for proposal message containing job requriements sent to the servers
	 * @param serverRequest transfer request message coming from the server
	 * @param jobTransfer   job for which the transfer is being performed
	 * @param jobClient     client which should be informed about job status updates
	 */
	public InitiateJobTransferRequest(final Agent agent,
			final ACLMessage cfp,
			final ACLMessage serverRequest,
			final PowerShortageJob jobTransfer,
			final String jobClient) {
		super(agent, cfp);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.jobTransfer = jobTransfer;
		this.serverRequest = serverRequest;
		this.jobClient = new AID(jobClient, AID.ISGUID);
	}

	/**
	 * Method processes Server Agent responses.
	 * It selects one server to which the job will be transferred.
	 * If no servers are available, it sends the information to server with power shortage that the transfer was
	 * unsuccessful.
	 *
	 * @param responses   retrieved responses from Server Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		MDC.put(MDC_JOB_ID, jobTransfer.getJobInstanceId().getJobId());
		if (responses.isEmpty()) {
			logger.info(SERVER_TRANSFER_NO_RESPONSE_LOG);
			respondWithFailureMessage();
		} else if (proposals.isEmpty()) {
			logger.info(SERVER_TRANSFER_NO_SERVERS_AVAILABLE_LOG);
			respondWithFailureMessage();
		} else {
			final List<ACLMessage> validProposals = retrieveValidMessages(proposals, ServerData.class);

			if (!validProposals.isEmpty()) {
				final ACLMessage chosenServerOffer = chooseServerToExecuteJob(validProposals);
				final ServerData chosenServerData = readMessageContent(chosenServerOffer, ServerData.class);
				final AID chosenServer = chosenServerOffer.getSender();
				logger.info(SERVER_TRANSFER_CHOSEN_SERVER_LOG, chosenServerData.getJobId(), chosenServer.getName());

				initiateTransferForServer(chosenServer, chosenServerOffer);
				rejectJobOffers(myCloudNetworkAgent, jobTransfer.getJobInstanceId(), chosenServerOffer, proposals);
			} else {
				handleInvalidResponses(proposals);
			}
		}
	}

	private void initiateTransferForServer(final AID chosenServer, final ACLMessage chosenOffer) {
		final ACLMessage replyToChosenOffer = prepareAcceptReplyWithProtocol(chosenOffer.createReply(),
				jobTransfer.getJobInstanceId(), POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL);
		final ACLMessage replyToServerRequest = prepareReply(serverRequest.createReply(),
				TRANSFER_SUCCESSFUL_MESSAGE, INFORM);

		displayMessageArrow(myCloudNetworkAgent, chosenServer);
		displayMessageArrow(myCloudNetworkAgent, serverRequest.getSender());

		myAgent.send(replyToChosenOffer);
		myCloudNetworkAgent.send(replyToServerRequest);
		myCloudNetworkAgent.addBehaviour(createFor(myCloudNetworkAgent, jobTransfer, chosenServer));
	}

	private void handleInvalidResponses(final List<ACLMessage> proposals) {
		rejectJobOffers(myCloudNetworkAgent, jobTransfer.getJobInstanceId(), null, proposals);
		respondWithFailureMessage();
	}

	private void respondWithFailureMessage() {
		final ACLMessage response = prepareReply(serverRequest.createReply(), NO_SERVER_AVAILABLE_CAUSE_MESSAGE,
				FAILURE);
		myCloudNetworkAgent.send(response);
	}

	private ACLMessage chooseServerToExecuteJob(final List<ACLMessage> serverOffers) {
		return serverOffers.stream().min(this::compareServerOffers).orElseThrow();
	}

	private int compareServerOffers(final ACLMessage serverOffer1, final ACLMessage serverOffer2) {
		final ServerData server1;
		final ServerData server2;
		try {
			server1 = JsonMapper.getMapper().readValue(serverOffer1.getContent(), ServerData.class);
			server2 = JsonMapper.getMapper().readValue(serverOffer2.getContent(), ServerData.class);
			return server1.getAvailablePower() - server2.getAvailablePower();
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
	}
}
