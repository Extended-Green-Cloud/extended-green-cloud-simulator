package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.ALL_PARTS_STARTED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_START_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_START_ON_TIME_LOG;
import static com.greencloud.application.agents.client.constants.ClientAgentConstants.MAX_TIME_DIFFERENCE;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_IN_CLOUD_JOB_ID;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.google.common.annotations.VisibleForTesting;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles update regarding job execution start
 */
public class HandleJobStartUpdate extends AbstractJobUpdateHandler {

	private static final Logger logger = getLogger(HandleJobStartUpdate.class);

	public HandleJobStartUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	/**
	 * Method updates job information along with duration in job status map. Furthermore, it verifies
	 * if the job started on time and logs appropriate information.
	 */
	@Override
	public void action() {
		final JobClientStatusEnum jobStatus = updateEnum.getJobStatus();
		final JobStatusUpdate jobUpdate = readMessageContent(message, JobStatusUpdate.class);
		measureTimeToRetrieveTheMessage(jobUpdate);

		if (message.getConversationId().equals(STARTED_IN_CLOUD_JOB_ID)) {
			myClient.setInCloud(true);
		}

		if (!myClient.isSplit()) {
			updateInformationOfJobStatusUpdate(jobUpdate);
			checkIfJobStartedOnTime(jobUpdate.getChangeTime(), myClient.getJobExecution().getJobSimulatedStart());
			return;
		}
		updateInformationOfJobPartStatusUpdate(jobUpdate);
		checkIfJobStartedOnTime(jobUpdate.getChangeTime(),
				myClient.getJobParts().get(jobUpdate.getJobInstance().getJobId()).getJobSimulatedStart());

		if (myClient.manage().checkIfAllPartsMatchStatus(jobStatus)) {
			MDC.put(MDC_JOB_ID, myClient.getJobExecution().getJob().getJobId());
			logger.info(ALL_PARTS_STARTED_LOG);
		}
	}

	@VisibleForTesting
	protected void checkIfJobStartedOnTime(final Instant startTime, final Instant jobStartTime) {
		final long timeDifference = MILLIS.between(jobStartTime, startTime);
		if (MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_START_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_START_DELAY_LOG, convertToRealTime(timeDifference));
		}
	}
}
