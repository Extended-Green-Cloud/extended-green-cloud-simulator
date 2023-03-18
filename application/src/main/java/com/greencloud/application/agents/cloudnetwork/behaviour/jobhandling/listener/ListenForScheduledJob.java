package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_CFP_NEW_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.NEW_JOB_REQUEST_TEMPLATE;
import static com.greencloud.application.agents.cloudnetwork.constants.CloudNetworkAgentConstants.MAX_MESSAGE_NUMBER_IN_BATCH;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateNewJobExecutorLookup;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles upcoming CFP from Scheduler Agent
 */
public class ListenForScheduledJob extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForScheduledJob.class);

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
	 * Method listens for the upcoming job CFP from the Scheduler Agent.
	 * It announces the jobs to the network by forwarding CFP with job characteristics to owned Server Agents.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(NEW_JOB_REQUEST_TEMPLATE, MAX_MESSAGE_NUMBER_IN_BATCH);

		if (Objects.nonNull(messages)) {
			messages.stream().parallel().forEach(message -> {
				final ClientJob job = readMessageContent(message, ClientJob.class);

				MDC.put(MDC_JOB_ID, job.getJobId());
				logger.info(SEND_CFP_NEW_LOG, job.getJobId());

				myCloudNetworkAgent.getNetworkJobs().put(job, PROCESSING);
				myAgent.addBehaviour(InitiateNewJobExecutorLookup.create(myCloudNetworkAgent, message, job));
			});
		} else {
			block();
		}
	}
}
