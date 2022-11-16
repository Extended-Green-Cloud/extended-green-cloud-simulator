package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CHOSEN_SERVER_FOR_JOB_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.INCORRECT_PROPOSAL_FORMAT_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVER_RESPONSES_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;
import static com.greencloud.application.messages.MessagingUtils.retrieveProposals;
import static com.greencloud.application.messages.domain.factory.OfferMessageFactory.makeJobOfferForClient;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.domain.CloudNetworkAgentConstants;
import com.greencloud.application.domain.ServerData;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.application.messages.MessagingUtils;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour initiates lookup for Server which will execute the Client's job
 */
public class InitiateNewJobExecutorLookup extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewJobExecutorLookup.class);

	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;
	private final ClientJob job;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent           agent which is executing the behaviour
	 * @param cfp             call for proposal message containing job requriements sent to the servers
	 * @param originalMessage original message received from the client
	 * @param job             job of interest
	 */
	public InitiateNewJobExecutorLookup(final Agent agent, final ACLMessage cfp, final ACLMessage originalMessage,
			ClientJob job) {
		super(agent, cfp);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.replyMessage = originalMessage.createReply();
		this.job = job;
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
		final List<ACLMessage> proposals = retrieveProposals(responses);

		MDC.put(MDC_JOB_ID, job.getJobId());
		if (responses.isEmpty()) {
			logger.info(NO_SERVER_RESPONSES_LOG);
			myCloudNetworkAgent.getNetworkJobs().remove(job);
			myAgent.send(prepareRefuseReply(replyMessage));
		} else if (proposals.isEmpty()) {
			myCloudNetworkAgent.getNetworkJobs().remove(job);
			myAgent.send(prepareRefuseReply(replyMessage));
		} else {
			final List<ACLMessage> validProposals = MessagingUtils.retrieveValidMessages(proposals, ServerData.class);
			if (!validProposals.isEmpty()) {
				final ACLMessage chosenServerOffer = chooseServerToExecuteJob(validProposals);
				final ServerData chosenServerData = MessagingUtils.readMessageContent(chosenServerOffer,
						ServerData.class);

				logger.info(CHOSEN_SERVER_FOR_JOB_LOG, job.getJobId(), chosenServerOffer.getSender().getName());

				final ACLMessage reply = chosenServerOffer.createReply();
				final ACLMessage offer = makeJobOfferForClient(chosenServerData,
						myCloudNetworkAgent.manage().getCurrentPowerInUse(), replyMessage);

				myCloudNetworkAgent.getServerForJobMap().put(job.getJobId(), chosenServerOffer.getSender());
				myCloudNetworkAgent.addBehaviour(new InitiateMakingNewJobOffer(myCloudNetworkAgent, offer, reply));
				rejectJobOffers(myCloudNetworkAgent, mapToJobInstanceId(job), chosenServerOffer, proposals);
			} else {
				handleInvalidResponses(proposals);
			}
		}
	}

	private void handleInvalidResponses(final List<ACLMessage> proposals) {
		logger.info(INCORRECT_PROPOSAL_FORMAT_LOG);
		rejectJobOffers(myCloudNetworkAgent, mapToJobInstanceId(job), null, proposals);
		myAgent.send(prepareRefuseReply(replyMessage));
	}

	private ACLMessage chooseServerToExecuteJob(final List<ACLMessage> serverOffers) {

		return serverOffers.stream().min(this::compareServerOffers).orElseThrow();
	}

	private int compareServerOffers(final ACLMessage serverOffer1, final ACLMessage serverOffer2) {
		ServerData server1;
		ServerData server2;
		int weight1 = myCloudNetworkAgent.getWeightsForServersMap().get(serverOffer1.getSender());
		int weight2 = myCloudNetworkAgent.getWeightsForServersMap().get(serverOffer2.getSender());
		try {
			server1 = getMapper().readValue(serverOffer1.getContent(), ServerData.class);
			server2 = getMapper().readValue(serverOffer2.getContent(), ServerData.class);
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
		int powerDifference = (server1.getAvailablePower() * weight1) - (server2.getAvailablePower() * weight2);
		int priceDifference = (int) ((server1.getServicePrice() * 1/weight1) - (server2.getServicePrice() * 1/weight2));
		return CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE.isValidIntValue(powerDifference) ?
				priceDifference :
				powerDifference;
	}
}
