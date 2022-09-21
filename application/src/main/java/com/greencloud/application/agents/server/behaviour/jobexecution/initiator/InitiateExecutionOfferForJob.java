package com.greencloud.application.agents.server.behaviour.jobexecution.initiator;

import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_REJECT_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static com.greencloud.application.utils.JobMapUtils.getJobByIdAndStartDate;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.JobWithProtocol;
import com.greencloud.application.messages.MessagingUtils;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

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
	 * @param accept_proposal accept proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept_proposal) {
		final JobWithProtocol jobWithProtocol = MessagingUtils.readMessageContent(accept_proposal, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
		final Job jobInstance = getJobByIdAndStartDate(myServerAgent.getServerJobs(), jobInstanceId);
		final Integer availableCapacity = myServerAgent.manage().getAvailableCapacity(jobInstance.getStartTime(), jobInstance.getEndTime(), null, null);
		if (jobInstance.getPower() > availableCapacity) {
			myServerAgent.getServerJobs().remove(jobInstance);
			myServerAgent.getGreenSourceForJobMap().remove(jobInstanceId.getJobId());
			myServerAgent.send(ReplyMessageFactory.prepareReply(replyMessage, jobInstanceId, REJECT_PROPOSAL));
			myServerAgent.send(ReplyMessageFactory.prepareFailureReply(accept_proposal.createReply(), jobInstanceId));
		} else {
			MDC.put(MDC_JOB_ID, jobInstance.getJobId());
			logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG);
			myServerAgent.getServerJobs().replace(jobInstance, JobStatusEnum.ACCEPTED);
			myAgent.send(ReplyMessageFactory.prepareAcceptReplyWithProtocol(replyMessage, jobInstanceId,
					jobWithProtocol.getReplyProtocol()));
		}
		myServerAgent.manage().updateClientNumberGUI();
		displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
	}

	/**
	 * Method handles REJECT_PROPOSAL response received from the Cloud Network Agents.
	 * It forwards the REJECT_PROPOSAL to the Green Source Agent
	 *
	 * @param reject_proposal reject proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject_proposal) {
		final JobInstanceIdentifier jobInstanceId = MessagingUtils.readMessageContent(reject_proposal,
				JobInstanceIdentifier.class);
		final Job job = getJobByIdAndStartDate(myServerAgent.getServerJobs(), jobInstanceId);
		myServerAgent.getGreenSourceForJobMap().remove(jobInstanceId.getJobId());
		myServerAgent.getServerJobs().remove(job);
		displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(SERVER_OFFER_REJECT_LOG, reject_proposal.getSender().getLocalName());
		myServerAgent.send(ReplyMessageFactory.prepareReply(replyMessage, jobInstanceId, REJECT_PROPOSAL));
	}
}
