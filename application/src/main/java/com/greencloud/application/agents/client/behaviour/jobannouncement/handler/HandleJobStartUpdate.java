package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_START_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_START_ON_TIME_LOG;
import static com.greencloud.application.agents.client.constants.ClientAgentConstants.MAX_TIME_DIFFERENCE;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;

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
		final JobStatusUpdate jobUpdate = readMessageContent(message, JobStatusUpdate.class);
		measureTimeToRetrieveTheMessage(jobUpdate);
		updateInformationOfJobStatusUpdate(jobUpdate);
		checkIfJobStartedOnTime(jobUpdate.getChangeTime(), myClient.getJobExecution().getJobSimulatedStart());

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
