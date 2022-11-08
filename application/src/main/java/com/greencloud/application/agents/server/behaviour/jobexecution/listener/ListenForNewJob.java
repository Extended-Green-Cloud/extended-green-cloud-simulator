package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.initiator.InitiatePowerDeliveryForJob;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.mapper.JobMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming job's CFP from cloud network com.greencloud.application.agents
 */
public class ListenForNewJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForNewJob.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts the agent to the ServerAgent.
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job CFP coming from the Cloud Network Agents.
	 * It validates whether the server has enough power to handle the job.
	 * If yes, then it sends the CFP to owned green sources to find available power sources.
	 * If no, then it sends the refuse message to the Cloud Network Agent.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(JobHandlingMessageTemplates.NEW_JOB_CFP_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			MDC.put(MDC_JOB_ID, job.getJobId());
			final int availableCapacity = myServerAgent.manage()
					.getAvailableCapacity(job.getStartTime(), job.getEndTime(), null, null);
			final boolean validJobConditions = job.getPower() <= availableCapacity &&
					!myServerAgent.getServerJobs().containsKey(job) &&
					myServerAgent.canTakeIntoProcessing();

			if (validJobConditions) {
				initiateNegotiationWithPowerSources(job, message);
			} else {
				logger.info(JobHandlingListenerLog.SERVER_NEW_JOB_LACK_OF_POWER_LOG);
				myAgent.send(prepareRefuseReply(message.createReply()));
			}
		} else {
			block();
		}
	}

	private void initiateNegotiationWithPowerSources(final ClientJob job, final ACLMessage cnaMessage) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(SERVER_NEW_JOB_LOOK_FOR_SOURCE_LOG);

		myServerAgent.getServerJobs().putIfAbsent(job, JobStatusEnum.PROCESSING);
		myServerAgent.tookJobIntoProcessing();

		final ACLMessage cfp = createCallForProposal(JobMapper.mapJobToPowerJob(job),
				myServerAgent.getOwnedGreenSources(), SERVER_JOB_CFP_PROTOCOL);

		myAgent.addBehaviour(new InitiatePowerDeliveryForJob(myAgent, cfp, cnaMessage.createReply(), job));
	}
}
