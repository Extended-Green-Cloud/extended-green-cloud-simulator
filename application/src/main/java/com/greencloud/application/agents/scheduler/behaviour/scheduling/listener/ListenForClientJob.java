package com.greencloud.application.agents.scheduler.behaviour.scheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.JOB_ALREADY_EXISTING_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.JOB_ENQUEUED_SUCCESSFULLY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.JOB_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.logs.JobSchedulingListenerLog.QUEUE_THRESHOLD_EXCEEDED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.listener.templates.JobSchedulingMessageTemplates.NEW_JOB_ANNOUNCEMENT_TEMPLATE;
import static com.greencloud.application.messages.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.CREATED;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for upcoming new client jobs
 */
public class ListenForClientJob extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForClientJob.class);

	private SchedulerAgent myScheduler;

	/**
	 * Method casts the abstract agent to the agent of type SchedulerAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		myScheduler = (SchedulerAgent) myAgent;
	}

	/**
	 * Method listens for the upcoming job announcement information messages coming from the Cloud Network.
	 * It evaluates the job priority and puts it into the job schedule queue.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_ANNOUNCEMENT_TEMPLATE);

		if (nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			final String jobId = job.getJobId();

			MDC.put(MDC_JOB_ID, jobId);
			logger.info(JOB_RECEIVED_LOG, jobId);

			if (myScheduler.getClientJobs().containsKey(job)) {
				MDC.put(MDC_JOB_ID, jobId);
				logger.info(JOB_ALREADY_EXISTING_LOG, job.getJobId(), myScheduler.getClientJobs().get(job));
				return;
			}
			putJobToQueue(job);

		} else {
			block();
		}
	}

	private void putJobToQueue(final ClientJob job) {
		myScheduler.getClientJobs().put(job, CREATED);
		if (myScheduler.getJobsToBeExecuted().offer(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(JOB_ENQUEUED_SUCCESSFULLY_LOG, job.getJobId());
			myScheduler.manage().updateJobQueueGUI();
			myScheduler.manage().sendStatusMessageToClient(prepareJobStatusMessageForClient(job, SCHEDULED_JOB_ID));
		} else {
			logger.info(QUEUE_THRESHOLD_EXCEEDED_LOG);
		}
	}

}
