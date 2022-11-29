package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.ACCEPT_SERVER_PROPOSAL_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.REJECT_SERVER_PROPOSAL_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import com.greencloud.commons.job.JobResultType;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.commons.job.ClientJob;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviour sends proposal with job execution offer to the Scheduler
 */
public class InitiateMakingNewJobOffer extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateMakingNewJobOffer.class);

	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent        agent which is executing the behaviour
	 * @param msg          proposal message with job execution price that will be sent to the scheduler
	 * @param replyMessage reply message sent to server with ACCEPT/REJECT proposal
	 */
	public InitiateMakingNewJobOffer(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
		super(agent, msg);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.replyMessage = replyMessage;
	}

	/**
	 * Method handles ACCEPT_PROPOSAL message retrieved from the Scheduler Agent.
	 * It sends accept proposal to the chosen for job execution Server Agent and updates the network state.
	 *
	 * @param accept received accept proposal message
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept) {
		final String jobId = accept.getContent();
		final ClientJob job = getJobById(jobId, myCloudNetworkAgent.getNetworkJobs());

		if (Objects.nonNull(job)) {
			MDC.put(MDC_JOB_ID, jobId);
			logger.info(ACCEPT_SERVER_PROPOSAL_LOG);

			myCloudNetworkAgent.manage().incrementJobCounter(jobId, JobResultType.ACCEPTED);
			myCloudNetworkAgent.manageConfig().saveMonitoringData();
			myAgent.send(
					prepareAcceptReplyWithProtocol(replyMessage, mapToJobInstanceId(job), SERVER_JOB_CFP_PROTOCOL));
		}
	}

	/**
	 * Method handles REJECT_PROPOSAL message retrieved from the Scheduler Agent.
	 * It sends reject proposal to the Server Agent previously chosen for the job execution.
	 *
	 * @param reject received reject proposal message
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject) {
		final String jobId = reject.getContent();
		final ClientJob job = getJobById(jobId, myCloudNetworkAgent.getNetworkJobs());
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(REJECT_SERVER_PROPOSAL_LOG, reject.getSender().getName());

		if (Objects.nonNull(job)) {
			myCloudNetworkAgent.getServerForJobMap().remove(jobId);
			myCloudNetworkAgent.getNetworkJobs().remove(getJobById(jobId, myCloudNetworkAgent.getNetworkJobs()));
			myCloudNetworkAgent.manageConfig().saveMonitoringData();
		}

		myCloudNetworkAgent.send(prepareReply(replyMessage, mapToJobInstanceId(job), REJECT_PROPOSAL));
	}
}
