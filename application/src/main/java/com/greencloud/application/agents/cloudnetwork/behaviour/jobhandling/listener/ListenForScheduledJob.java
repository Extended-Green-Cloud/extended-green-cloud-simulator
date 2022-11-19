package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_CFP_NEW_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.NEW_JOB_REQUEST_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateNewJobExecutorLookup;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming call for proposals from Scheduler Agent
 */
public class ListenForScheduledJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForScheduledJob.class);

	private CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Method casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job call for proposals from the Scheduler Agent.
	 * It announces the job to the network by sending call for proposal with job characteristics to owned Server Agents.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_REQUEST_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			sendCallForProposalToServers(job, message);
		} else {
			block();
		}
	}

	private void sendCallForProposalToServers(final ClientJob job, final ACLMessage message) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(SEND_CFP_NEW_LOG, job.getJobId());
		final ACLMessage cfp = createCallForProposal(job, myCloudNetworkAgent.getOwnedServers(),
				CNA_JOB_CFP_PROTOCOL);

		myCloudNetworkAgent.getNetworkJobs().put(job, JobStatusEnum.PROCESSING);
		myAgent.addBehaviour(new InitiateNewJobExecutorLookup(myAgent, cfp, message, job));
	}
}
