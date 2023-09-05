package com.greencloud.application.agents.scheduler.behaviour.scheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.JOB_FAILED_RETRY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.JOB_UPDATE_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.SCHEDULER_MESSAGE_BATCH;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for job updates coming from the Cloud Networks
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForJobUpdate.class);

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
		final List<ACLMessage> messages = myAgent.receive(JOB_UPDATE_TEMPLATE, SCHEDULER_MESSAGE_BATCH);

		if (nonNull(messages)) {
			messages.stream().parallel().forEach(message -> {
				final JobStatusUpdate jobStatusUpdate = readMessageContent(message, JobStatusUpdate.class);

				MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
				logger.info(JOB_UPDATE_RECEIVED_LOG, jobStatusUpdate.getJobInstance().getJobId());
				handleJobStatusChange(jobStatusUpdate, message.getConversationId());
			});
		} else {
			block();
		}
	}

	private void handleJobStatusChange(final JobStatusUpdate jobStatusUpdate, final String type) {
		final ClientJob job = getJobById(jobStatusUpdate.getJobInstance().getJobId(), mySchedulerAgent.getClientJobs());

		if (nonNull(job)) {
			switch (type) {
				case STARTED_JOB_ID -> mySchedulerAgent.getClientJobs().replace(job, PROCESSING, IN_PROGRESS);
				case FINISH_JOB_ID -> mySchedulerAgent.manage().handleJobCleanUp(job);
				case FAILED_JOB_ID -> handleJobFailure(jobStatusUpdate, job);
			}
			if (!type.equals(FAILED_JOB_ID)) {
				mySchedulerAgent.manage()
						.sendStatusMessageToClient(prepareJobStatusMessageForClient(job, jobStatusUpdate, type));
			}
		}
	}

	private void handleJobFailure(final JobStatusUpdate jobStatusUpdate, final ClientJob job) {
		if (mySchedulerAgent.manage().postponeJobExecution(job)) {
			logger.info(JOB_FAILED_RETRY_LOG, job.getJobId());
			mySchedulerAgent.manage().sendStatusMessageToClient(preparePostponeJobMessageForClient(job));
		} else {
			mySchedulerAgent.manage().jobFailureCleanUp(job);
			mySchedulerAgent.manage().sendStatusMessageToClient(prepareJobStatusMessageForClient(job, jobStatusUpdate,
					FAILED_JOB_ID));
		}
	}
}
