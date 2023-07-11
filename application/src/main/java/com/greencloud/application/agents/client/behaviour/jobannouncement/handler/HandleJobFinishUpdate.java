package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.ALL_PARTS_FINISHED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_FINISHED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_FINISH_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_FINISH_ON_TIME_LOG;
import static com.greencloud.application.agents.client.constants.ClientAgentConstants.MAX_TIME_DIFFERENCE;
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
import com.greencloud.application.agents.client.domain.ClientJobExecution;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles update regarding job execution finish
 */
public class HandleJobFinishUpdate extends AbstractJobUpdateHandler {

	private static final Logger logger = getLogger(HandleJobFinishUpdate.class);

	public HandleJobFinishUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	private static void shutdownAfterFinishedJob(final ClientAgent client, final String logMessage) {
		MDC.put(MDC_JOB_ID, client.getJobExecution().getJob().getJobId());
		logger.info(logMessage);
		client.getGuiController().updateClientsCountByValue(-1);
		client.getGuiController().updateFinishedJobsCountByValue(1);

		if(client.isInCloud()) {
			client.getGuiController().updateJobsFinishedInCloudCountByValue(1);
		}

		client.manage().writeClientData(true);
		client.doDelete();
	}

	/**
	 * Method updates job information along with duration in job status map.
	 * Furthermore, it verifies if the job finished on time and logs appropriate information.
	 * In case of finishing entire job execution (i.e. for jobs that were split) the agent terminates.
	 */
	@Override
	public void action() {
		final JobClientStatusEnum jobStatus = updateEnum.getJobStatus();
		final JobStatusUpdate jobUpdate = readMessageContent(message, JobStatusUpdate.class);
		measureTimeToRetrieveTheMessage(jobUpdate);

		if (!myClient.isSplit()) {
			updateInformationOfJobStatusUpdate(jobUpdate);
			checkIfJobFinishedOnTime(jobUpdate.getChangeTime(), myClient.getJobExecution().getJobSimulatedEnd(),
					myClient.getJobExecution().getJobSimulatedDeadline(), jobUpdate.getJobInstance().getJobId());
			shutdownAfterFinishedJob(myClient, CLIENT_JOB_FINISHED_LOG);
			return;
		}
		updateInformationOfJobPartStatusUpdate(jobUpdate);

		final String jobPartId = jobUpdate.getJobInstance().getJobId();
		final ClientJobExecution jobPart = myClient.getJobParts().get(jobPartId);
		checkIfJobFinishedOnTime(jobUpdate.getChangeTime(), jobPart.getJobSimulatedEnd(),
				jobPart.getJobSimulatedDeadline(), jobUpdate.getJobInstance().getJobId());
		myClient.manage().writeClientData(false);

		if (myClient.manage().checkIfAllPartsMatchStatus(jobStatus)) {
			MDC.put(MDC_JOB_ID, myClient.getJobExecution().getJob().getJobId());
			checkIfJobFinishedOnTime(jobUpdate.getChangeTime(), myClient.getJobExecution().getJobSimulatedEnd(),
					myClient.getJobExecution().getJobSimulatedDeadline(), jobUpdate.getJobInstance().getJobId());
			shutdownAfterFinishedJob(myClient, ALL_PARTS_FINISHED_LOG);
		}
	}

	@VisibleForTesting
	protected void checkIfJobFinishedOnTime(final Instant endTime, final Instant jobEndTime,
			final Instant jobDeadline, final String jobId) {
		if (!jobDeadline.isBefore(endTime)) {
			final long timeDifference = MILLIS.between(endTime, jobEndTime);
			final long delay = -1 * convertToRealTime(timeDifference);

			if (delay == 0) {
				logger.info(CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG, jobId);
			} else {
				logger.info(CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_DELAY_LOG, jobId, delay);
			}
		} else {
			final long deadlineDifference = MILLIS.between(endTime, jobDeadline);

			if (MAX_TIME_DIFFERENCE.isValidValue(deadlineDifference)) {
				logger.info(CLIENT_JOB_FINISH_ON_TIME_LOG, jobId);
			} else {
				logger.info(CLIENT_JOB_FINISH_DELAY_LOG, jobId, deadlineDifference);
			}
		}
	}
}
