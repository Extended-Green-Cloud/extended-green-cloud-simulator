package com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_FAILED_IN_CLOUD_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_FAILED_RETRY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_UPDATE_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_POSTPONE_LIMIT;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.SCHEDULER_MESSAGE_BATCH;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.job.execution.handler.HandleJobStartInCloud;
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
				case FINISH_JOB_ID -> mySchedulerAgent.manage().handleJobCleanUp(job, true);
				case FAILED_JOB_ID -> handleJobFailure(jobStatusUpdate, job);
			}
			if (!type.equals(FAILED_JOB_ID)) {
				mySchedulerAgent.manage()
						.sendStatusMessageToClient(prepareJobStatusMessageForClient(job, jobStatusUpdate, type),
								job.getJobId());
			}
		}
	}

	private void handleJobFailure(final JobStatusUpdate jobStatusUpdate, final ClientJob job) {
		// if the job execution will exceed the deadline -> FAIL
		if (!mySchedulerAgent.manage().canJobBeFullyExecutedBeforeDeadline(job)) {
			mySchedulerAgent.manage().jobFailureCleanUp(job);
			mySchedulerAgent.manage().sendStatusMessageToClient(prepareJobStatusMessageForClient(job, jobStatusUpdate,
					FAILED_JOB_ID), job.getJobId());

			// if the job cannot be postponed due to deadline or the max number of job retries was exceeded -> EXECUTE IN CLOUD
		} else if (mySchedulerAgent.manage().isJobAfterDeadline(job)
				|| mySchedulerAgent.getJobPostpones().get(job.getJobId()) >= JOB_POSTPONE_LIMIT) {
			logger.info(JOB_FAILED_IN_CLOUD_LOG, job.getJobId());
			mySchedulerAgent.getClientJobs().replace(job, ACCEPTED);
			mySchedulerAgent.getGuiController().updateAllJobsCountByValue(1);
			mySchedulerAgent.addBehaviour(HandleJobStartInCloud.createFor(mySchedulerAgent, job));

			// if the job can be postponed -> POSTPONE AND TRY IN CLOUD
		} else {
			mySchedulerAgent.manage().postponeJobExecution(job);
			logger.info(JOB_FAILED_RETRY_LOG, job.getJobId());
			mySchedulerAgent.manage()
					.sendStatusMessageToClient(preparePostponeJobMessageForClient(job), job.getJobId());

			final int jobPostpones = mySchedulerAgent.getJobPostpones().get(job.getJobId()) + 1;
			mySchedulerAgent.getJobPostpones().replace(job.getJobId(), jobPostpones);
		}
	}
}
