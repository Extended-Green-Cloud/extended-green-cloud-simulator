package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.JOB_FAILED_RETRY_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.logs.JobSchedulingListenerLog.JOB_UPDATE_RECEIVED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.domain.job.JobStatusEnum.PROCESSING;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.util.Objects.isNull;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
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
			final String jobId = message.getContent();
			MDC.put(MDC_JOB_ID, jobId);
			logger.info(JOB_UPDATE_RECEIVED_LOG, jobId);
			handleJobStatusChange(jobId, message.getConversationId());
		} else {
			block();
		}
	}

	private void handleJobStatusChange(final String jobId, final String type) {
		final ClientJob job = mySchedulerAgent.manage().getJobById(jobId);

		if (isNull(job)) {
			// do nothing
			return;
		}

		switch (type) {
			case STARTED_JOB_ID -> mySchedulerAgent.getClientJobs().replace(job, PROCESSING, IN_PROGRESS);
			case FINISH_JOB_ID -> handleJobCleanUp(job, type);
			case FAILED_JOB_ID -> handleJobFailure(job);
		}
		if (!type.equals(FAILED_JOB_ID)) {
			final ACLMessage messageToClient = prepareJobStatusMessageForClient(job.getClientIdentifier(), jobId, type);
			mySchedulerAgent.send(messageToClient);
		}
	}

	private void handleJobFailure(final ClientJob job) {
		if (mySchedulerAgent.manage().postponeJobExecution(job)) {
			logger.info(JOB_FAILED_RETRY_LOG, job.getJobId());
			mySchedulerAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), job.getJobId(),
					POSTPONED_JOB_ID));
		} else {
			handleJobCleanUp(job, FAILED_JOB_ID);
			mySchedulerAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), job.getJobId(),
					FAILED_JOB_ID));
		}
	}

	private void handleJobCleanUp(final ClientJob job, final String type) {
		mySchedulerAgent.getClientJobs().remove(job);
		mySchedulerAgent.getCnaForJobMap().remove(job.getJobId());
		mySchedulerAgent.getJobParts().values()
				.stream()
				.filter(j -> j.getJobId().equals(job.getJobId()))
				.findFirst()
				.ifPresent(jobPart -> handleJobPartStatusChange(job.getJobId(), jobPart, type));
	}

	private void handleJobPartStatusChange(String jobId, ClientJob jobPart, String type) {
		var originalJobId = jobId.split("#")[0];
		mySchedulerAgent.getJobParts().remove(originalJobId, jobPart);
		if (type.equals(FAILED_JOB_ID)) {
			// TODO handle canceling rest of the job parts in CNA, SERVER and GREEN ENERGY agents - next pull request
		}
	}
}
