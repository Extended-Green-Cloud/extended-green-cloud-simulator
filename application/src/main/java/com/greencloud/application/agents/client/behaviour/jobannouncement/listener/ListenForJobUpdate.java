package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_BACK_UP_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FAILED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_GREEN_POWER_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_ON_HOLD_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_POSTPONE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_PROCESSED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_SCHEDULED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.postponeTime;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.JobStatusEnum.SCHEDULED;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.ClientAgentConstants;
import com.greencloud.commons.job.JobStatusEnum;
import com.gui.agents.ClientAgentNode;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles the information that the job status has been updated
 */
public class ListenForJobUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobUpdate.class);

	private final ClientAgent myClientAgent;

	/**
	 * Behaviours constructor.
	 *
	 * @param clientAgent agent executing the behaviour
	 */
	public ListenForJobUpdate(final ClientAgent clientAgent) {
		super(clientAgent);
		this.myClientAgent = clientAgent;
	}

	/**
	 * Method which waits for messages informing about changes in the job's status
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE);

		if (Objects.nonNull(message)) {
			switch (message.getConversationId()) {
				case SCHEDULED_JOB_ID -> {
					logger.info(CLIENT_JOB_SCHEDULED_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(SCHEDULED);
				}
				case PROCESSING_JOB_ID -> {
					logger.info(CLIENT_JOB_PROCESSED_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(PROCESSED);
				}
				case STARTED_JOB_ID -> {
					checkIfJobStartedOnTime();
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.IN_PROGRESS);
				}
				case DELAYED_JOB_ID -> {
					logger.info(CLIENT_JOB_DELAY_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.DELAYED);
				}
				case BACK_UP_POWER_JOB_ID -> {
					logger.info(CLIENT_JOB_BACK_UP_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.ON_BACK_UP);
				}
				case GREEN_POWER_JOB_ID -> {
					logger.info(CLIENT_JOB_GREEN_POWER_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.IN_PROGRESS);
				}
				case ON_HOLD_JOB_ID -> {
					logger.info(CLIENT_JOB_ON_HOLD_LOG);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.ON_HOLD);
				}
				case FINISH_JOB_ID -> {
					checkIfJobFinishedOnTime();
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.FINISHED);
					myClientAgent.getGuiController().updateClientsCountByValue(-1);
					myClientAgent.doDelete();
				}
				case POSTPONED_JOB_ID -> {
					logger.info(CLIENT_JOB_POSTPONE_LOG);
					myClientAgent.retry();
					myClientAgent.setSimulatedJobStart(postponeTime(myClientAgent.getSimulatedJobStart(), JOB_RETRY_MINUTES_ADJUSTMENT));
					myClientAgent.setSimulatedJobEnd(postponeTime(myClientAgent.getSimulatedJobEnd(), JOB_RETRY_MINUTES_ADJUSTMENT));
				}
				case FAILED_JOB_ID -> {
					logger.info(CLIENT_JOB_FAILED_LOG);
					myClientAgent.getGuiController().updateClientsCountByValue(-1);
					((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.FAILED);
					myClientAgent.doDelete();
				}
			}
		} else {
			block();
		}
	}

	private void checkIfJobStartedOnTime() {
		final Instant startTime = getCurrentTime();
		final long timeDifference = ChronoUnit.MILLIS.between(myClientAgent.getSimulatedJobStart(), startTime);
		if (ClientAgentConstants.MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_START_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_START_DELAY_LOG, timeDifference);
		}
	}

	private void checkIfJobFinishedOnTime() {
		final Instant endTime = getCurrentTime();
		final long timeDifference = ChronoUnit.MILLIS.between(endTime, myClientAgent.getSimulatedJobEnd());
		if (ClientAgentConstants.MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_FINISH_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_FINISH_DELAY_LOG, timeDifference);
		}
	}
}
