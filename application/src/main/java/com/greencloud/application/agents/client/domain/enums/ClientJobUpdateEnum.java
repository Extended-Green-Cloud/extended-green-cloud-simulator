package com.greencloud.application.agents.client.domain.enums;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.CLIENT_JOB_SPLIT_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_BACK_UP_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FAILED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_GREEN_POWER_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_ON_HOLD_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_POSTPONE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_PROCESSED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_RESCHEDULED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_SCHEDULED_LOG;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.DELAYED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.FINISHED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.ON_HOLD;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.PROCESSED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.SCHEDULED;

import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;

/**
 * Enum storing all statuses assigned for given message types.
 * <p> IMPORTANT! When adding new status update type it is crucial to match the enum name with conversation identifier
 * passed in the message sent to the Client </p>
 */
public enum ClientJobUpdateEnum {

	SCHEDULED_JOB_ID(SCHEDULED, CLIENT_JOB_SCHEDULED_LOG),
	PROCESSING_JOB_ID(PROCESSED, CLIENT_JOB_PROCESSED_LOG),
	DELAYED_JOB_ID(DELAYED, CLIENT_JOB_DELAY_LOG),
	BACK_UP_POWER_JOB_ID(ON_BACK_UP, CLIENT_JOB_BACK_UP_LOG),
	GREEN_POWER_JOB_ID(IN_PROGRESS, CLIENT_JOB_GREEN_POWER_LOG),
	ON_HOLD_JOB_ID(ON_HOLD, CLIENT_JOB_ON_HOLD_LOG),
	STARTED_JOB_ID(IN_PROGRESS, null),
	STARTED_IN_CLOUD_JOB_ID(IN_PROGRESS, null),
	FINISH_JOB_ID(FINISHED, null),
	FINISH_IN_CLOUD_JOB_ID(FINISHED, null),
	FAILED_JOB_ID(FAILED, CLIENT_JOB_FAILED_LOG),
	POSTPONED_JOB_ID(null, CLIENT_JOB_POSTPONE_LOG),
	SPLIT_JOB_ID(null, CLIENT_JOB_SPLIT_LOG),
	RE_SCHEDULED_JOB_ID(null, CLIENT_JOB_RESCHEDULED_LOG);

	private final JobClientStatusEnum jobStatus;
	private final String logMessage;

	ClientJobUpdateEnum(final JobClientStatusEnum jobStatus, final String logMessage) {
		this.jobStatus = jobStatus;
		this.logMessage = logMessage;
	}

	public JobClientStatusEnum getJobStatus() {
		return jobStatus;
	}

	public String getLogMessage() {
		return logMessage;
	}
}
