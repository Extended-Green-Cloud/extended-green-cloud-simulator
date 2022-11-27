package com.greencloud.application.agents.server.behaviour.jobexecution.initiator;

import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_FAILURE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_REJECT_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_BY_SERVER;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDate;
import static com.greencloud.commons.job.JobResultType.ACCEPTED;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobWithProtocol;
import com.greencloud.commons.job.ClientJob;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviours sends job execution offer to Cloud Network Agent and handles received responses
 */
public class InitiateExecutionOfferForJob extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateExecutionOfferForJob.class);

	private final ServerAgent myServerAgent;
	private final ACLMessage replyMessage;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent        agent executing the behaviour
	 * @param msg          proposal message that has to be sent to the Cloud Network
	 * @param replyMessage reply message sent to chosen Green Source after receiving the Cloud Network
	 *                     response
	 */
	public InitiateExecutionOfferForJob(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
		super(agent, msg);
		this.replyMessage = replyMessage;
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method handles ACCEPT_PROPOSAL response received from the Cloud Network Agents.
	 * It schedules the job execution, updates the network data and responds with ACCEPT_PROPOSAL to the
	 * chosen Green Source Agent
	 *
	 * @param accept accept proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept) {
		final JobWithProtocol jobWithProtocol = readMessageContent(accept, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
		final ClientJob jobInstance = getJobByIdAndStartDate(jobInstanceId, myServerAgent.getServerJobs());
		if (Objects.nonNull(jobInstance)) {
			final int availableCapacity = myServerAgent.manage()
					.getAvailableCapacity(jobInstance.getStartTime(), jobInstance.getEndTime(), null, null);
			myServerAgent.manage().incrementJobCounter(jobInstanceId, ACCEPTED);
			myServerAgent.getServerJobs().replace(jobInstance, ACCEPTED_BY_SERVER);

			if (jobInstance.getPower() > availableCapacity) {
				passJobExecutionFailure(jobInstance, jobInstanceId, jobWithProtocol.getReplyProtocol(), accept);
			} else {
				acceptGreenSourceForJobExecution(jobInstanceId, jobWithProtocol);
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
		final JobInstanceIdentifier jobInstanceId = readMessageContent(reject,
				JobInstanceIdentifier.class);
		final ClientJob job = getJobByIdAndStartDate(jobInstanceId, myServerAgent.getServerJobs());
		myServerAgent.getGreenSourceForJobMap().remove(jobInstanceId.getJobId());

		if (Objects.nonNull(job)) {
			myServerAgent.getServerJobs().remove(job);
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(SERVER_OFFER_REJECT_LOG, reject.getSender().getLocalName());
			myServerAgent.send(prepareReply(replyMessage, jobInstanceId, REJECT_PROPOSAL));
		}
	}

	private void passJobExecutionFailure(final ClientJob jobInstance, final JobInstanceIdentifier jobInstanceId,
			final String protocol, final ACLMessage cnaAccept) {
		final String responseProtocol = protocol.equals(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL) ?
				FAILED_TRANSFER_PROTOCOL :
				FAILED_JOB_PROTOCOL;

		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_FAILURE_LOG, jobInstance.getJobId());
		myServerAgent.getServerJobs().remove(jobInstance);
		myServerAgent.getGreenSourceForJobMap().remove(jobInstance.getJobId());
		myServerAgent.manage().incrementJobCounter(jobInstanceId, FAILED);

		myServerAgent.send(prepareReply(replyMessage, jobInstanceId, REJECT_PROPOSAL));
		myServerAgent.send(prepareFailureReply(cnaAccept.createReply(), jobInstanceId, responseProtocol));
	}

	private void acceptGreenSourceForJobExecution(final JobInstanceIdentifier jobInstanceId,
			final JobWithProtocol jobWithProtocol) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG);
		myAgent.send(
				prepareAcceptReplyWithProtocol(replyMessage, jobInstanceId, jobWithProtocol.getReplyProtocol()));
	}
}
