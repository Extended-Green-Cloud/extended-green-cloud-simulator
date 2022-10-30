package com.greencloud.application.agents.client.behaviour.jobannouncement.initiator;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.INVALID_CLOUD_PROPOSAL_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.NO_CLOUD_AVAILABLE_NO_RETRY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.NO_CLOUD_AVAILABLE_RETRY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.NO_CLOUD_RESPONSES_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.SEND_ACCEPT_TO_CLOUD_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.logs.JobAnnouncementInitiatorLog.SEND_CFP_TO_CLOUD_LOG;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.CLOUD_NETWORK_AGENTS;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.MAX_RETRIES;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.MAX_TRAFFIC_DIFFERENCE;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.RETRY_PAUSE_MILLISECONDS;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;
import static com.greencloud.application.messages.MessagingUtils.retrieveProposals;
import static com.greencloud.application.messages.MessagingUtils.retrieveValidMessages;
import static com.greencloud.application.messages.domain.constants.MessageContentConstants.INVALID_JOB_ID_MESSAGE;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CLIENT_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.jobannouncement.handler.HandleClientJobRequestRetry;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.PricedJob;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.application.messages.domain.constants.MessageContentConstants;
import com.greencloud.commons.job.JobStatusEnum;
import com.gui.agents.ClientAgentNode;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour sends and handles job's call for proposal
 */
public class InitiateNewJobAnnouncement extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewJobAnnouncement.class);

	private final transient ClientJob job;
	private final ClientAgent myClientAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent agent executing the behaviour
	 * @param cfp   call for proposal message containing job details that will be sent to Cloud Network Agents
	 * @param job   the job that the client want to be executed
	 */
	public InitiateNewJobAnnouncement(final Agent agent, final ACLMessage cfp, final ClientJob job) {
		super(agent, cfp);
		this.myClientAgent = (ClientAgent) agent;
		this.job = job;
	}

	/**
	 * Method prepares the call for proposal message.
	 *
	 * @param callForProposal default call for proposal message
	 * @return vector containing the call for proposals with job characteristics sent to the Cloud Network Agents
	 */
	@Override
	protected Vector prepareCfps(final ACLMessage callForProposal) {
		logger.info(SEND_CFP_TO_CLOUD_LOG);
		final Vector<ACLMessage> vector = new Vector<>();
		final List<AID> cloudNetworks = (List<AID>) getParent().getDataStore().get(CLOUD_NETWORK_AGENTS);
		vector.add(createCallForProposal(job, cloudNetworks, CLIENT_JOB_CFP_PROTOCOL));
		return vector;
	}

	/**
	 * Method handles the responses retrieved from the Cloud Network Agents.
	 * It selects one Cloud Network Agent that will execute the job and rejects the remaining ones.
	 *
	 * @param responses   all retrieved Cloud Network Agents' responses
	 * @param acceptances vector containing accept proposal message that will be sent back to the chosen
	 *                    Cloud Network Agent
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		if (responses.isEmpty()) {
			logger.info(NO_CLOUD_RESPONSES_LOG);
			myAgent.doDelete();
		} else if (proposals.isEmpty()) {
			handleRetryProcess();
		} else {
			List<ACLMessage> validProposals = retrieveValidMessages(proposals, PricedJob.class);

			if (!validProposals.isEmpty()) {
				final ACLMessage chosenOffer = chooseCNAToExecuteJob(validProposals);
				final PricedJob pricedJob = readMessageContent(chosenOffer, PricedJob.class);
				logger.info(SEND_ACCEPT_TO_CLOUD_LOG, chosenOffer.getSender().getName());

				myClientAgent.setChosenCloudNetworkAgent(chosenOffer.getSender());
				acceptances.add(prepareStringReply(chosenOffer.createReply(), pricedJob.getJobId(), ACCEPT_PROPOSAL));
				rejectJobOffers(myClientAgent, pricedJob.getJobId(), chosenOffer, proposals);
			} else {
				handleInvalidProposals(proposals);
			}
		}
	}

	private void handleInvalidProposals(final List<ACLMessage> proposals) {
		logger.info(INVALID_CLOUD_PROPOSAL_LOG);
		myClientAgent.getGuiController().updateClientsCountByValue(-1);
		((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.REJECTED);
		rejectJobOffers(myClientAgent, INVALID_JOB_ID_MESSAGE, null, proposals);
	}

	private void handleRetryProcess() {
		if (myClientAgent.getRetries() < MAX_RETRIES) {
			logger.info(NO_CLOUD_AVAILABLE_RETRY_LOG, myClientAgent.getRetries());
			myClientAgent.retry();
			myClientAgent.addBehaviour(new HandleClientJobRequestRetry(myAgent, RETRY_PAUSE_MILLISECONDS, job));
		} else {
			logger.info(NO_CLOUD_AVAILABLE_NO_RETRY_LOG);
			myClientAgent.getGuiController().updateClientsCountByValue(-1);
			((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.REJECTED);
		}
	}

	private ACLMessage chooseCNAToExecuteJob(final List<ACLMessage> receivedOffers) {
		return receivedOffers.stream().min(this::compareCNAOffers).orElseThrow();
	}

	private int compareCNAOffers(final ACLMessage cnaOffer1, final ACLMessage cnaOffer2) {
		try {
			final PricedJob cna1 = readMessageContent(cnaOffer1, PricedJob.class);
			final PricedJob cna2 = readMessageContent(cnaOffer2, PricedJob.class);

			double powerDifference = cna1.getPowerInUse() - cna2.getPowerInUse();
			int priceDifference = (int) (cna1.getPriceForJob() - cna2.getPriceForJob());
			return MAX_TRAFFIC_DIFFERENCE.isValidIntValue((int) powerDifference) ?
					priceDifference :
					(int) powerDifference;

		} catch (IncorrectMessageContentException e) {
			return Integer.MAX_VALUE;
		}
	}
}
