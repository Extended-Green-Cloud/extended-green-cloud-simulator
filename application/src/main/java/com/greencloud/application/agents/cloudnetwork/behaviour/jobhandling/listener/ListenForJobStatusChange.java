package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.JOB_CONFIRMED_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_BACK_UP_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_GREEN_POWER_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_FAILED_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_FINISH_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_START_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_ON_HOLD_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.JOB_STATUS_CHANGE_TEMPLATE;
import static com.greencloud.application.agents.cloudnetwork.domain.CloudNetworkAgentConstants.MAX_ERROR_IN_JOB_START;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.HandleDelayedJob;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.utils.TimeUtils;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour returns to the Scheduler, job status update
 */
public class ListenForJobStatusChange extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobStatusChange.class);

	private CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Method casts the abstract agent to the agent of type CloudNetworkAgent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
	}

	/**
	 * Method listens for the information regarding new job status.
	 * It passes that information to the scheduler.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(JOB_STATUS_CHANGE_TEMPLATE);

		if (Objects.nonNull(message)) {
			final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
			final String jobId = jobInstanceId.getJobId();

			if (Objects.nonNull(myCloudNetworkAgent.manage().getJobById(jobId))) {
				MDC.put(MDC_JOB_ID, jobId);

				if (message.getProtocol().equals(CHANGE_JOB_STATUS_PROTOCOL)) {
					switch (message.getConversationId()) {
						case CONFIRMED_JOB_ID -> handleConfirmedJobMessage(jobId);
						case STARTED_JOB_ID -> handleStartedJobMessage(jobId);
						case FINISH_JOB_ID -> handleFinishJobMessage(jobId);
						default -> handleJobStatusUpdateMessage(jobId, message.getConversationId());
					}
				} else if (message.getProtocol().equals(FAILED_JOB_PROTOCOL)) {
					handleFailedJobMessage(jobId);
				}
			}
		} else {
			block();
		}
	}

	private void handleConfirmedJobMessage(final String jobId) {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		MDC.put(MDC_JOB_ID, jobId);
		logger.info(JOB_CONFIRMED_STATUS_LOG, jobId);
		myCloudNetworkAgent.getNetworkJobs().replace(job, ACCEPTED);
		myAgent.addBehaviour(new HandleDelayedJob(myCloudNetworkAgent, calculateExpectedJobStart(job), job.getJobId()));
	}

	private void handleStartedJobMessage(final String jobId) {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		if (!myCloudNetworkAgent.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			MDC.put(MDC_JOB_ID, jobId);
			logger.info(SEND_JOB_START_STATUS_LOG, jobId);
			myCloudNetworkAgent.getNetworkJobs().replace(myCloudNetworkAgent.manage().getJobById(jobId), IN_PROGRESS);
			myCloudNetworkAgent.manage().incrementStartedJobs(jobId);
			myAgent.send(prepareJobStatusMessageForScheduler(myCloudNetworkAgent, jobId, STARTED_JOB_ID));
		}
	}

	private void handleFinishJobMessage(final String jobId) {
		final Long completedJobs = myCloudNetworkAgent.completedJob();
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(SEND_JOB_FINISH_STATUS_LOG, jobId, completedJobs);
		updateNetworkInformation(jobId);
		myAgent.send(prepareJobStatusMessageForScheduler(myCloudNetworkAgent, jobId, FINISH_JOB_ID));
	}

	private void handleJobStatusUpdateMessage(final String jobId, String type) {
		MDC.put(MDC_JOB_ID, jobId);
		switch (type) {
			case ON_HOLD_JOB_ID -> logger.info(SEND_ON_HOLD_STATUS_LOG, jobId);
			case GREEN_POWER_JOB_ID -> logger.info(SEND_GREEN_POWER_STATUS_LOG, jobId);
			case BACK_UP_POWER_JOB_ID -> logger.info(SEND_BACK_UP_STATUS_LOG, jobId);
		}
		myAgent.send(prepareJobStatusMessageForScheduler(myCloudNetworkAgent, jobId, type));
	}

	private void handleFailedJobMessage(final String jobId) {
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(SEND_JOB_FAILED_STATUS_LOG, jobId);
		myCloudNetworkAgent.getNetworkJobs().remove(myCloudNetworkAgent.manage().getJobById(jobId));
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myAgent.send(prepareJobStatusMessageForScheduler(myCloudNetworkAgent, jobId, FAILED_JOB_ID));
	}

	private void updateNetworkInformation(final String jobId) {
		myCloudNetworkAgent.getNetworkJobs().remove(myCloudNetworkAgent.manage().getJobById(jobId));
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.manage().incrementFinishedJobs(jobId);
	}

	private Date calculateExpectedJobStart(final ClientJob job) {
		final Instant startTime = TimeUtils.getCurrentTime().isAfter(job.getStartTime()) ?
				TimeUtils.getCurrentTime() :
				job.getStartTime();
		return Date.from(startTime.plusSeconds(MAX_ERROR_IN_JOB_START));
	}
}
