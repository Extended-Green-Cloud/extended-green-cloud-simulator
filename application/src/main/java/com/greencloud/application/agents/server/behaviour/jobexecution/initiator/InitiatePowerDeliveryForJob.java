package com.greencloud.application.agents.server.behaviour.jobexecution.initiator;

import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.messages.MessagingUtils;
import com.greencloud.application.messages.domain.factory.OfferMessageFactory;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviour searches for available green source that can supply new job with power
 */
public class InitiatePowerDeliveryForJob extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiatePowerDeliveryForJob.class);

	private final ACLMessage replyMessage;
	private final ServerAgent myServerAgent;
	private final ClientJob job;

	/**
	 * Behaviour constructor
	 *
	 * @param agent        agent which executed the behaviour
	 * @param powerRequest call for proposal containing the details regarding power needed to execute the job
	 * @param replyMessage reply message sent to cloud network after receiving the green sources' responses
	 */
	public InitiatePowerDeliveryForJob(final Agent agent, final ACLMessage powerRequest, final ACLMessage replyMessage,
			final ClientJob job) {
		super(agent, powerRequest);
		this.replyMessage = replyMessage;
		this.job = job;
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method waits for all Green Source Agent responses.
	 * It analyzes received proposals, selects the Green Source Agent which will be the power supplier for new job
	 * and rejects the remaining ones.
	 *
	 * @param responses   retrieved responses from Green Source Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen green source (not used)
	 */
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		final List<ACLMessage> proposals = MessagingUtils.retrieveProposals(responses);

		MDC.put(MDC_JOB_ID, job.getJobId());
		myServerAgent.stoppedJobProcessing();
		if (responses.isEmpty()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_RESPONSE_LOG);
			refuseToExecuteJob(proposals);
		} else if (proposals.isEmpty()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_SOURCES_AVAILABLE_LOG);
			refuseToExecuteJob(proposals);
		} else if (myServerAgent.manage().getAvailableCapacity(job.getStartTime(), job.getEndTime(), null, null)
				<= job.getPower()) {
			logger.info(NEW_JOB_LOOK_FOR_GS_NO_POWER_AVAILABLE_LOG);
			refuseToExecuteJob(proposals);
		} else {
			final List<ACLMessage> validProposals = MessagingUtils.retrieveValidMessages(proposals,
					GreenSourceData.class);
			final boolean isJobStillProcessed = myServerAgent.getServerJobs().containsKey(job);

			if (!validProposals.isEmpty() && isJobStillProcessed) {
				final ACLMessage chosenGreenSourceOffer = myServerAgent.chooseGreenSourceToExecuteJob(validProposals);
				proposeServerOffer(chosenGreenSourceOffer);
				rejectJobOffers(myServerAgent, mapToJobInstanceId(job), chosenGreenSourceOffer, proposals);
			} else {
				handleInvalidProposals(proposals);
			}
		}
		myServerAgent.removeBehaviour(this);
	}

	private void proposeServerOffer(final ACLMessage chosenOffer) {
		final AID chosenGreenSource = chosenOffer.getSender();
		final GreenSourceData offerData = MessagingUtils.readMessageContent(chosenOffer, GreenSourceData.class);
		final String jobId = offerData.getJobId();
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(NEW_JOB_LOOK_FOR_GS_SELECTED_GS_LOG, jobId, chosenGreenSource.getLocalName());

		final double servicePrice = myServerAgent.manage().calculateServicePrice(offerData);
		final ACLMessage proposalMessage = OfferMessageFactory.makeServerJobOffer(myServerAgent, servicePrice, jobId,
				replyMessage);
		myServerAgent.getGreenSourceForJobMap().put(jobId, chosenGreenSource);
		myServerAgent.addBehaviour(
				new InitiateExecutionOfferForJob(myAgent, proposalMessage, chosenOffer.createReply()));
	}

	private void handleInvalidProposals(final List<ACLMessage> proposals) {
		rejectJobOffers(myServerAgent, mapToJobInstanceId(job), null, proposals);
		refuseToExecuteJob(proposals);
	}

	private void refuseToExecuteJob(final List<ACLMessage> proposals) {
		myServerAgent.getServerJobs().remove(job);
		myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
		rejectJobOffers(myServerAgent, mapToJobInstanceId(job), null, proposals);
	}
}
