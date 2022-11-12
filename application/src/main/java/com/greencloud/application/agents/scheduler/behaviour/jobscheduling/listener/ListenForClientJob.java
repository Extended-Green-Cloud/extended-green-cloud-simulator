package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.JOB_ALREADY_EXISTING_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.JOB_ENQUEUED_SUCCESSFULLY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.QUEUE_THRESHOLD_EXCEEDED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.templates.JobSchedulingMessageTemplates.NEW_JOB_ANNOUNCEMENT_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.CREATED;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for upcoming new client jobs
 */
public class ListenForClientJob extends CyclicBehaviour {

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
	 * Method listens for the upcoming job announcement information messages coming from the Cloud Network.
	 * It evaluates the job priority and puts it into the job schedule queue.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(NEW_JOB_ANNOUNCEMENT_TEMPLATE);

		if (Objects.nonNull(message)) {
			final ClientJob job = readMessageContent(message, ClientJob.class);
			final String jobId = job.getJobId();
			MDC.put(MDC_JOB_ID, jobId);
			addJobToPriorityQueue(job, message.getSender().getName());
		} else {
			block();
		}
	}

	private void addJobToPriorityQueue(final ClientJob job, final String client) {
		if (mySchedulerAgent.getClientJobs().containsKey(job)) {
			logger.info(JOB_ALREADY_EXISTING_LOG, job.getJobId(), mySchedulerAgent.getClientJobs().get(job));
			return;
		}
		mySchedulerAgent.getClientJobs().put(job, CREATED);
		if (mySchedulerAgent.getJobsToBeExecuted().offer(job)) {
			logger.info(JOB_ENQUEUED_SUCCESSFULLY_LOG, job.getJobId());
			mySchedulerAgent.send(prepareJobStatusMessageForClient(client, SCHEDULED_JOB_ID));
		} else {
			logger.info(QUEUE_THRESHOLD_EXCEEDED_LOG);
		}
	}
}
