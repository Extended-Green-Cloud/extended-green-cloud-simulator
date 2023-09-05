package com.greencloud.application.agents.server.behaviour.jobexecution.initiator;

import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_FAILURE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_REJECT_LOG;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.factory.OfferMessageFactory.prepareServerJobOffer;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED_BY_SERVER;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.agent.GreenSourceData;
import com.greencloud.application.domain.job.ImmutableJobStatusUpdate;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.application.domain.job.JobWithProtocol;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.resources.HardwareResources;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviours sends job execution offer to Cloud Network Agent and handles received responses
 */
public class InitiateExecutionOfferForJob extends ProposeInitiator {

	private static final Logger logger = getLogger(InitiateExecutionOfferForJob.class);

	private final ServerAgent myServerAgent;
	private final ACLMessage greenSourceMessage;

	private InitiateExecutionOfferForJob(final Agent agent, final ACLMessage msg, final ACLMessage greenSourceMessage) {
		super(agent, msg);

		this.greenSourceMessage = greenSourceMessage;
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent            agent executing the behaviour
	 * @param greenSourceOffer offer received from Green Source
	 * @param cnaMessage       original message received from Cloud Network
	 * @return InitiateExecutionOfferForJob
	 */
	public static InitiateExecutionOfferForJob create(final ServerAgent agent, final ACLMessage greenSourceOffer,
			final ACLMessage cnaMessage) {
		final GreenSourceData data = readMessageContent(greenSourceOffer, GreenSourceData.class);
		final double servicePrice = agent.manage().calculateServicePrice(data);
		final ACLMessage proposalMessage = prepareServerJobOffer(agent, servicePrice, data.getJobId(), cnaMessage);

		return new InitiateExecutionOfferForJob(agent, proposalMessage, greenSourceOffer);
	}

	/**
	 * Method handles ACCEPT_PROPOSAL response received from the Cloud Network Agents.
	 * It recomputes the available resources to verify if job can be executed and if not - it responds with FAILURE,
	 * or otherwise it communicates with selected Green Source to received confirmation regarding energy supply.
	 *
	 * @param accept accept proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept) {
		final JobWithProtocol jobWithProtocol = readMessageContent(accept, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstance = jobWithProtocol.getJobInstanceIdentifier();
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			final HardwareResources availableResources =
					myServerAgent.resources().getAvailableResources(job, null, null);

			myServerAgent.manage().incrementJobCounter(jobInstance, ACCEPTED);
			myServerAgent.getServerJobs().replace(job, ACCEPTED_BY_SERVER);

			if (!availableResources.areSufficient(job.getEstimatedResources())) {
				passJobExecutionFailure(job, jobInstance, jobWithProtocol.getReplyProtocol(), accept);
			} else {
				acceptGreenSourceForJobExecution(jobInstance, jobWithProtocol.getReplyProtocol());
			}
		}
	}

	/**
	 * Method handles REJECT_PROPOSAL response received from the Cloud Network Agents.
	 * It forwards the REJECT_PROPOSAL to the Green Source Agent
	 *
	 * @param reject reject proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject) {
		final JobInstanceIdentifier jobInstance = readMessageContent(reject, JobInstanceIdentifier.class);
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(SERVER_OFFER_REJECT_LOG, reject.getSender().getLocalName());
			handleJobRejection(job, jobInstance);
		}
	}

	private void passJobExecutionFailure(final ClientJob job, final JobInstanceIdentifier jobInstance,
			final String protocol, final ACLMessage cnaAccept) {
		final String responseProtocol = protocol.equals(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL) ?
				FAILED_TRANSFER_PROTOCOL : FAILED_JOB_PROTOCOL;
		final JobStatusUpdate jobStatusUpdate = new ImmutableJobStatusUpdate(jobInstance, getCurrentTime());

		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_FAILURE_LOG, job.getJobId());

		handleJobRejection(job, jobInstance);
		myServerAgent.manage().incrementJobCounter(jobInstance, FAILED);
		myServerAgent.send(prepareFailureReply(cnaAccept, jobStatusUpdate, responseProtocol));
	}

	private void acceptGreenSourceForJobExecution(final JobInstanceIdentifier jobInstance, final String protocol) {
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG);
		myAgent.send(prepareAcceptJobOfferReply(greenSourceMessage, jobInstance, protocol));
	}

	private void handleJobRejection(final ClientJob job, final JobInstanceIdentifier jobInstance) {
		myServerAgent.getServerJobs().remove(job);
		myServerAgent.getGreenSourceForJobMap().remove(job.getJobId());
		myServerAgent.send(prepareReply(greenSourceMessage, jobInstance, REJECT_PROPOSAL));
	}
}
