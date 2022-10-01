package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CHOSEN_SERVER_FOR_JOB_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.INCORRECT_PROPOSAL_FORMAT_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVERS_AVAILABLE_RETRIES_LIMIT_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVERS_AVAILABLE_RETRY_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVER_RESPONSES_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.HandleJobRequestRetry;
import com.greencloud.application.agents.cloudnetwork.domain.CloudNetworkAgentConstants;
import com.greencloud.application.domain.ServerData;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.messages.MessagingUtils;
import com.greencloud.application.messages.domain.factory.OfferMessageFactory;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour initiates lookup for server which will execute the client's job
 */
public class InitiateNewJobExecutorLookup extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewJobExecutorLookup.class);

	private final ACLMessage originalMessage;
	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;
	private final String jobId;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent           agent which is executing the behaviour
	 * @param cfp             call for proposal message containing job requriements sent to the servers
	 * @param originalMessage original message received from the client
	 */
	public InitiateNewJobExecutorLookup(final Agent agent, final ACLMessage cfp, final ACLMessage originalMessage,
			String jobId) {
		super(agent, cfp);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.originalMessage = originalMessage;
		this.replyMessage = originalMessage.createReply();
		this.jobId = jobId;
	}

	/**
	 * Method waits for all Server Agent responses.
	 * It chooses the Server Agent for job execution and rejects the remaining ones.
	 *
	 * @param responses   retrieved responses from Server Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		final List<ACLMessage> proposals = MessagingUtils.retrieveProposals(responses);

		MDC.put(MDC_JOB_ID, jobId);
		if (responses.isEmpty()) {
			logger.info(NO_SERVER_RESPONSES_LOG);
		} else if (proposals.isEmpty()) {
			initiateRetryProcess();
		} else {
			final List<ACLMessage> validProposals = MessagingUtils.retrieveValidMessages(proposals, ServerData.class);
			if (!validProposals.isEmpty()) {
				final ACLMessage chosenServerOffer = chooseServerToExecuteJob(validProposals);
				final ServerData chosenServerData = MessagingUtils.readMessageContent(chosenServerOffer,
						ServerData.class);
				final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

				logger.info(CHOSEN_SERVER_FOR_JOB_LOG, jobId, chosenServerOffer.getSender().getName());

				final ACLMessage reply = chosenServerOffer.createReply();
				final ACLMessage offer = OfferMessageFactory.makeJobOfferForClient(chosenServerData,
						myCloudNetworkAgent.manage().getCurrentPowerInUse(), replyMessage);

				myCloudNetworkAgent.getServerForJobMap().put(jobId, chosenServerOffer.getSender());
				myCloudNetworkAgent.addBehaviour(new InitiateMakingNewJobOffer(myCloudNetworkAgent, offer, reply));
				MessagingUtils.rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job),
						chosenServerOffer, proposals);
			} else {
				handleInvalidResponses(proposals);
			}
		}
	}

	private void handleInvalidResponses(final List<ACLMessage> proposals) {
		logger.info(INCORRECT_PROPOSAL_FORMAT_LOG);
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);
		MessagingUtils.rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job), null, proposals);
		myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
	}

	private void initiateRetryProcess() {
		int retries = myCloudNetworkAgent.getJobRequestRetries().get(jobId);

		if (retries >= CloudNetworkAgentConstants.RETRY_LIMIT) {
			logger.info(NO_SERVERS_AVAILABLE_RETRIES_LIMIT_LOG);
			myCloudNetworkAgent.getJobRequestRetries().remove(jobId);
			myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
		} else {
			myCloudNetworkAgent.getJobRequestRetries().put(jobId, ++retries);
			logger.info(NO_SERVERS_AVAILABLE_RETRY_LOG, retries);
			myAgent.addBehaviour(
					new HandleJobRequestRetry(myCloudNetworkAgent, CloudNetworkAgentConstants.RETRY_PAUSE_MILLISECONDS,
							originalMessage, jobId));
		}
	}

	private ACLMessage chooseServerToExecuteJob(final List<ACLMessage> serverOffers) {
		return serverOffers.stream().min(this::compareServerOffers).orElseThrow();
	}

	private int compareServerOffers(final ACLMessage serverOffer1, final ACLMessage serverOffer2) {
		ServerData server1;
		ServerData server2;
		try {
			server1 = JsonMapper.getMapper().readValue(serverOffer1.getContent(), ServerData.class);
			server2 = JsonMapper.getMapper().readValue(serverOffer2.getContent(), ServerData.class);
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
		int powerDifference = server1.getAvailablePower() - server2.getAvailablePower();
		int priceDifference = (int) (server1.getServicePrice() - server2.getServicePrice());
		return CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE.isValidIntValue(powerDifference) ?
				priceDifference :
				powerDifference;
	}
}
