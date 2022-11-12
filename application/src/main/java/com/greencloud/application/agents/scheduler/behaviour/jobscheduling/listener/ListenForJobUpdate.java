package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.JOB_UPDATE_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.domain.job.JobStatusEnum.PROCESSING;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for job updates coming from the Cloud Networks
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForClientJob.class);

	private SchedulerAgent mySchedulerAgent;

	/**
	 * Method casts the abstract agent to the agent of type SchedulerAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		mySchedulerAgent = (SchedulerAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job status changes and, according to them, updates
	 * the internal state and passes the information to clients
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(JOB_UPDATE_TEMPLATE);

		if (Objects.nonNull(message)) {
			final String jobId = readMessageContent(message, String.class);
			MDC.put(MDC_JOB_ID, jobId);
			logger.info(JOB_UPDATE_RECEIVED_LOG, jobId);
			handleJobStatusChange(jobId, message.getConversationId());
		} else {
			block();
		}
	}

	private void handleJobStatusChange(final String jobId, final String type) {
		final ClientJob job = mySchedulerAgent.manage().getJobById(jobId);

		if (Objects.nonNull(job)) {
			if (type.equals(STARTED_JOB_ID)) {
				mySchedulerAgent.getClientJobs().replace(job, PROCESSING, IN_PROGRESS);
			} else if (List.of(FINISH_JOB_ID, FAILED_JOB_ID).contains(type)) {
				mySchedulerAgent.getClientJobs().remove(job);
				mySchedulerAgent.getCnaForJobMap().remove(jobId);
			}
			final ACLMessage messageToClient = prepareJobStatusMessageForClient(job.getClientIdentifier(), type);
			mySchedulerAgent.send(messageToClient);
		}
	}
}
