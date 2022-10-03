package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_GREEN_POWER_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_FINISH_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.logs.JobHandlingListenerLog.SEND_JOB_START_STATUS_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.listener.templates.JobHandlingMessageTemplates.JOB_STATUS_CHANGE_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.GREEN_POWER_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.JobMapUtils.getJobById;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour returns to the client job status update
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
	 * It passes that information to the client.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(JOB_STATUS_CHANGE_TEMPLATE);

		if (Objects.nonNull(message)) {
			final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
			final String jobId = jobInstanceId.getJobId();

			if (Objects.nonNull(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId))) {
				MDC.put(MDC_JOB_ID, jobId);
				switch (message.getProtocol()) {
					case FINISH_JOB_PROTOCOL -> handleFinishJobMessage(jobId);
					case STARTED_JOB_PROTOCOL -> handleStartedJobMessage(jobId);
					case POWER_SHORTAGE_FINISH_ALERT_PROTOCOL -> handleGreenPowerJobMessage(jobId);
				}
			}
		} else {
			block();
		}
	}

	private void handleGreenPowerJobMessage(final String jobId) {
		final ClientJob job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
		logger.info(SEND_GREEN_POWER_STATUS_LOG, jobId);
		myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), GREEN_POWER_JOB_PROTOCOL));
	}

	private void handleStartedJobMessage(final String jobId) {
		final ClientJob job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);

		if (!myCloudNetworkAgent.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.info(SEND_JOB_START_STATUS_LOG, jobId);
			myCloudNetworkAgent.getNetworkJobs().replace(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId), IN_PROGRESS);
			myCloudNetworkAgent.manage().incrementStartedJobs(jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), STARTED_JOB_PROTOCOL));
		}
	}

	private void handleFinishJobMessage(final String jobId) {
		final Long completedJobs = myCloudNetworkAgent.completedJob();
		logger.info(SEND_JOB_FINISH_STATUS_LOG, jobId, completedJobs);
		final String clientId = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId).getClientIdentifier();
		updateNetworkInformation(jobId);
		myAgent.send(prepareJobStatusMessageForClient(clientId, FINISH_JOB_PROTOCOL));
	}

	private void updateNetworkInformation(final String jobId) {
		myCloudNetworkAgent.getNetworkJobs().remove(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId));
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.manage().incrementFinishedJobs(jobId);
	}
}
