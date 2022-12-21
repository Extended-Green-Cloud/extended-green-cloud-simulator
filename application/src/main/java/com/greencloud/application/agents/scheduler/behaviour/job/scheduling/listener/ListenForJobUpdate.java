package com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_FAILED_RETRY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_UPDATE_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PROCESSING;
import static java.util.Objects.isNull;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for job updates coming from the Cloud Networks
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobUpdate.class);

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
			final JobStatusUpdate jobStatusUpdate = readMessageContent(message, JobStatusUpdate.class);
			MDC.put(MDC_JOB_ID, jobStatusUpdate.jobInstance().getJobId());
			logger.info(JOB_UPDATE_RECEIVED_LOG, jobStatusUpdate.jobInstance().getJobId());
			handleJobStatusChange(jobStatusUpdate, message.getConversationId());
			MDC.clear();
		} else {
			block();
		}
	}

	private void handleJobStatusChange(final JobStatusUpdate jobStatusUpdate, final String type) {
		final ClientJob job = getJobById(jobStatusUpdate.jobInstance().getJobId(), mySchedulerAgent.getClientJobs());

		if (isNull(job)) {
			// do nothing
			return;
		}

		switch (type) {
			case STARTED_JOB_ID -> mySchedulerAgent.getClientJobs().replace(job, PROCESSING, IN_PROGRESS);
			case FINISH_JOB_ID -> mySchedulerAgent.manage().handleJobCleanUp(job);
			case FAILED_JOB_ID -> handleJobFailure(jobStatusUpdate, job);
		}
		if (!type.equals(FAILED_JOB_ID)) {
			final ACLMessage messageToClient = prepareJobStatusMessageForClient(job.getClientIdentifier(),
					jobStatusUpdate, type);
			mySchedulerAgent.send(messageToClient);
		}
	}

	private void handleJobFailure(final JobStatusUpdate jobStatusUpdate, final ClientJob job) {
		if (mySchedulerAgent.manage().postponeJobExecution(job)) {
			logger.info(JOB_FAILED_RETRY_LOG, job.getJobId());
			mySchedulerAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), jobStatusUpdate,
					POSTPONED_JOB_ID));
		} else {
			mySchedulerAgent.manage().handleFailedJobCleanUp(job, parent);
			mySchedulerAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), jobStatusUpdate,
					FAILED_JOB_ID));
		}
	}
}
