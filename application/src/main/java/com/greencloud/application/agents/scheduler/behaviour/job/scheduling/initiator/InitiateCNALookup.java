package com.greencloud.application.agents.scheduler.behaviour.job.scheduling.initiator;

import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.MAX_TRAFFIC_DIFFERENCE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.PROCESSING;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;
import static com.greencloud.application.messages.MessagingUtils.retrieveProposals;
import static com.greencloud.application.messages.MessagingUtils.retrieveValidMessages;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.initiator.logs.JobSchedulingInitiatorLog;
import com.greencloud.application.domain.job.PricedJob;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.commons.job.ClientJob;

import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour looks for the Cloud Network that will handle the job execution
 */
public class InitiateCNALookup extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateCNALookup.class);
	private final SchedulerAgent myScheduler;
	private final ClientJob job;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent scheduler agent executing behaviour
	 * @param cfp   call for proposal sent to the Cloud Networks
	 * @param job   job of interest
	 */
	public InitiateCNALookup(final SchedulerAgent agent, final ACLMessage cfp, final ClientJob job) {
		super(agent, cfp);
		this.myScheduler = agent;
		this.job = job;
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
		MDC.put(MDC_JOB_ID, job.getJobId());

		if (responses.isEmpty()) {
			logger.info(JobSchedulingInitiatorLog.NO_CLOUD_RESPONSES_LOG);
			handleFailure();
		} else if (proposals.isEmpty()) {
			handleFailure();
		} else {
			List<ACLMessage> validProposals = retrieveValidMessages(proposals, PricedJob.class);

			if (!validProposals.isEmpty()) {
				final ACLMessage chosenOffer = chooseCNAToExecuteJob(validProposals);
				final PricedJob pricedJob = readMessageContent(chosenOffer, PricedJob.class);
				logger.info(JobSchedulingInitiatorLog.SEND_ACCEPT_TO_CLOUD_LOG, chosenOffer.getSender().getName());

				myScheduler.getClientJobs().replace(job, PROCESSING, ACCEPTED);
				myScheduler.getCnaForJobMap().put(job.getJobId(), chosenOffer.getSender());

				myScheduler.send(prepareStringReply(chosenOffer.createReply(), pricedJob.getJobId(), ACCEPT_PROPOSAL));
				rejectJobOffers(myScheduler, pricedJob.getJobId(), chosenOffer, proposals);
			} else {
				handleInvalidProposals(proposals);
			}
		}
		((ParallelBehaviour) parent).removeSubBehaviour(this);
	}

	private void handleFailure() {
		if (myScheduler.manage().postponeJobExecution(job)) {
			logger.info(JobSchedulingInitiatorLog.NO_CLOUD_AVAILABLE_RETRY_LOG);
			myScheduler.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), job.getJobId(),
					POSTPONED_JOB_ID));
		} else {
			logger.info(JobSchedulingInitiatorLog.NO_CLOUD_AVAILABLE_NO_RETRY_LOG);
			myScheduler.manage().handleFailedJobCleanUp(job, parent);
			myScheduler.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), job.getJobId(),
					FAILED_JOB_ID));
		}
	}

	private void handleInvalidProposals(final List<ACLMessage> proposals) {
		logger.info(JobSchedulingInitiatorLog.INVALID_CLOUD_PROPOSAL_LOG);
		rejectJobOffers(myScheduler, job.getJobId(), null, proposals);
		handleFailure();
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
