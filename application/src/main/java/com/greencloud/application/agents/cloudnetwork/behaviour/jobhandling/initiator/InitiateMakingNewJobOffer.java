package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.REJECT_SERVER_PROPOSAL_LOG;
import static com.greencloud.application.agents.cloudnetwork.domain.CloudNetworkAgentConstants.MAX_ERROR_IN_JOB_START;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.HandleDelayedJob;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.utils.TimeUtils;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviour sends proposal with job execution offer to the client
 */
public class InitiateMakingNewJobOffer extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateMakingNewJobOffer.class);

	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent        agent which is executing the behaviour
	 * @param msg          proposal message with job execution price that will be sent to the client
	 * @param replyMessage reply message sent to server with ACCEPT/REJECT proposal
	 */
	public InitiateMakingNewJobOffer(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
		super(agent, msg);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.replyMessage = replyMessage;
	}

	/**
	 * Method handles ACCEPT_PROPOSAL message retrieved from the Client Agent.
	 * It sends accept proposal to the chosen for job execution Server Agent and updates the network state.
	 *
	 * @param accept_proposal received accept proposal message
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept_proposal) {
		logger.info(JobHandlingInitiatorLog.ACCEPT_SERVER_PROPOSAL_LOG);
		final String jobId = accept_proposal.getContent();
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		MDC.put(MDC_JOB_ID, jobId);
		myCloudNetworkAgent.getNetworkJobs().replace(job, JobStatusEnum.ACCEPTED);
		myAgent.addBehaviour(new HandleDelayedJob(myCloudNetworkAgent, calculateExpectedJobStart(job), job.getJobId()));

		displayMessageArrow(myCloudNetworkAgent, replyMessage.getAllReceiver());
		myAgent.send(prepareAcceptReplyWithProtocol(replyMessage, mapToJobInstanceId(job), SERVER_JOB_CFP_PROTOCOL));
	}

	/**
	 * Method handles REJECT_PROPOSAL message retrieved from the Client Agent.
	 * It sends reject proposal to the Server Agent previously chosen for the job execution.
	 *
	 * @param reject_proposal received reject proposal message
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject_proposal) {
		logger.info(REJECT_SERVER_PROPOSAL_LOG, reject_proposal.getSender().getName());
		final String jobId = reject_proposal.getContent();
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.getNetworkJobs().remove(myCloudNetworkAgent.manage().getJobById(jobId));

		displayMessageArrow(myCloudNetworkAgent, replyMessage.getAllReceiver());
		myCloudNetworkAgent.send(prepareReply(replyMessage, mapToJobInstanceId(job), REJECT_PROPOSAL));
	}

	private Date calculateExpectedJobStart(final ClientJob job) {
		final Instant startTime = TimeUtils.getCurrentTime().isAfter(job.getStartTime()) ?
				TimeUtils.getCurrentTime() :
				job.getStartTime();
		return Date.from(startTime.plusSeconds(MAX_ERROR_IN_JOB_START));
	}
}
