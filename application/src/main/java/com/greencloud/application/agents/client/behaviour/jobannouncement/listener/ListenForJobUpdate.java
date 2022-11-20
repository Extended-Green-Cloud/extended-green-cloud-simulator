package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.ALL_PARTS_FINISHED;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.ALL_PARTS_STARTED;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_BACK_UP_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FAILED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISHED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_FINISH_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_GREEN_POWER_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_ON_HOLD_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_POSTPONE_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_PROCESSED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_RESCHEDULED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_SCHEDULED_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_SPLIT_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_DELAY_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.CLIENT_JOB_START_ON_TIME_LOG;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.logs.JobAnnouncementListenerLog.PART_FAILED;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.MAX_TIME_DIFFERENCE;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SPLIT_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.postponeTime;
import static com.greencloud.commons.job.JobStatusEnum.CREATED;
import static com.greencloud.commons.job.JobStatusEnum.DELAYED;
import static com.greencloud.commons.job.JobStatusEnum.FINISHED;
import static com.greencloud.commons.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.JobStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.job.JobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.JobStatusEnum.SCHEDULED;
import static java.util.Objects.isNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.domain.job.JobTimeFrames;
import com.greencloud.application.domain.job.SplitJob;
import com.greencloud.commons.job.ClientJob;
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

	private ClientAgentNode myNode;

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
		if (isNull(myNode)) {
			myNode = (ClientAgentNode) myClientAgent.getAgentNode();
		}

		final ACLMessage message = myAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE);

		if (Objects.nonNull(message)) {
			if (myClientAgent.isSplit()) {
				MDC.put(MDC_JOB_ID, message.getContent());
			}

			switch (message.getConversationId()) {
				case SCHEDULED_JOB_ID -> processBasedOnStatus(message, SCHEDULED, CLIENT_JOB_SCHEDULED_LOG);
				case PROCESSING_JOB_ID -> processBasedOnStatus(message, PROCESSED, CLIENT_JOB_PROCESSED_LOG);
				case STARTED_JOB_ID -> processStartedJob(message);
				case DELAYED_JOB_ID -> processBasedOnStatus(message, DELAYED, CLIENT_JOB_DELAY_LOG);
				case BACK_UP_POWER_JOB_ID -> processBasedOnStatus(message, ON_BACK_UP, CLIENT_JOB_BACK_UP_LOG);
				case GREEN_POWER_JOB_ID -> processBasedOnStatus(message, IN_PROGRESS, CLIENT_JOB_GREEN_POWER_LOG);
				case ON_HOLD_JOB_ID -> processBasedOnStatus(message, ON_HOLD, CLIENT_JOB_ON_HOLD_LOG);
				case FINISH_JOB_ID -> processFinishedJob(message);
				case POSTPONED_JOB_ID -> processPostponedJob(message);
				case FAILED_JOB_ID -> processFailedJob();
				case SPLIT_JOB_ID -> handleJobSplitting(message);
				case RE_SCHEDULED_JOB_ID -> processRescheduledJob(message);
			}
		} else {
			block();
		}
	}

	private void processBasedOnStatus(ACLMessage message, JobStatusEnum status, String logMessage) {
		logger.info(logMessage);
		if (!myClientAgent.isSplit()) {
			myNode.updateJobStatus(status);
			myClientAgent.setCurrentJobStatus(status);
			return;
		}
		processJobPartBasedOnStatus(message, status);
		updateOriginalStatus(status);
	}

	private void processStartedJob(ACLMessage message) {
		if (!myClientAgent.isSplit()) {
			myNode.updateJobStatus(IN_PROGRESS);
			myClientAgent.setCurrentJobStatus(IN_PROGRESS);
			checkIfJobStartedOnTime(myClientAgent.getSimulatedJobStart());
			return;
		}

		var jobPartId = message.getContent();
		processJobPartBasedOnStatus(message, IN_PROGRESS);
		checkIfJobStartedOnTime(myClientAgent.getJobParts().get(jobPartId).getSimulatedJobStart());
		updateOriginalStatus(IN_PROGRESS);

		if (checkIfAllPartsMatchStatus(IN_PROGRESS)) {
			MDC.put(MDC_JOB_ID, myClientAgent.getMyJob().getJobId());
			logger.info(ALL_PARTS_STARTED);
		}
	}

	private void processFinishedJob(ACLMessage message) {
		if (!myClientAgent.isSplit()) {
			checkIfJobFinishedOnTime(myClientAgent.getSimulatedJobEnd(), myClientAgent.getSimulatedDeadline());
			myNode.updateJobStatus(FINISHED);
			myClientAgent.setCurrentJobStatus(FINISHED);
			shutdownAfterFinishedJob(CLIENT_JOB_FINISHED_LOG);
			return;
		}
		var jobPartId = message.getContent();
		var jobPart = myClientAgent.getJobParts().get(jobPartId);
		checkIfJobFinishedOnTime(jobPart.getSimulatedJobEnd(), jobPart.getSimulatedDeadline());
		processJobPartBasedOnStatus(message, FINISHED);
		updateOriginalStatus(FINISHED);

		if (checkIfAllPartsMatchStatus(FINISHED)) {
			MDC.put(MDC_JOB_ID, myClientAgent.getMyJob().getJobId());
			checkIfJobFinishedOnTime(myClientAgent.getSimulatedJobEnd(), myClientAgent.getSimulatedDeadline());
			shutdownAfterFinishedJob(ALL_PARTS_FINISHED);
		}
	}

	private void processPostponedJob(ACLMessage message) {
		logger.info(CLIENT_JOB_POSTPONE_LOG);
		var postponedStart = postponeTime(myClientAgent.getSimulatedJobStart(), JOB_RETRY_MINUTES_ADJUSTMENT);
		var postponedEnd = postponeTime(myClientAgent.getSimulatedJobEnd(), JOB_RETRY_MINUTES_ADJUSTMENT);
		if (!myClientAgent.isSplit()) {
			myClientAgent.setSimulatedJobStart(postponedStart);
			myClientAgent.setSimulatedJobEnd(postponedEnd);
			return;
		}

		var jobPartId = message.getContent();
		var jobPart = myClientAgent.getJobParts().get(jobPartId);
		jobPart.setSimulatedJobStart(postponedStart);
		jobPart.setSimulatedJobEnd(postponedEnd);
	}

	private void processFailedJob() {
		logger.info(CLIENT_JOB_FAILED_LOG);
		if (myClientAgent.isSplit()) {
			MDC.put(MDC_JOB_ID, myClientAgent.getMyJob().getJobId());
			logger.info(PART_FAILED);
		}
		myClientAgent.getGuiController().updateClientsCountByValue(-1);
		myClientAgent.getGuiController().updateFailedJobsCountByValue(1);
		((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.FAILED);
		myClientAgent.doDelete();
	}

	private void processRescheduledJob(ACLMessage message) {
		final JobTimeFrames newTimeFrames = readMessageContent(message, JobTimeFrames.class);
		if (!myClientAgent.isSplit()) {
			logger.info(CLIENT_JOB_RESCHEDULED_LOG);
			myClientAgent.setSimulatedJobStart(newTimeFrames.getNewJobStart());
			myClientAgent.setSimulatedJobEnd(newTimeFrames.getNewJobEnd());
			return;
		}
		var jobPart = myClientAgent.getJobParts().get(newTimeFrames.getJobId());
		jobPart.setSimulatedJobStart(newTimeFrames.getNewJobStart());
		jobPart.setSimulatedJobEnd(newTimeFrames.getNewJobEnd());
	}

	private void processJobPartBasedOnStatus(ACLMessage message, JobStatusEnum status) {
		var jobPartId = message.getContent();
		myClientAgent.getJobParts().get(jobPartId).setStatus(status);
		myNode.updateJobStatus(status, jobPartId);
	}

	private boolean checkIfAllPartsMatchStatus(final JobStatusEnum status) {
		return myClientAgent.getJobParts().values().stream().map(JobPart::getStatus).allMatch(status::equals);
	}

	private void checkIfJobStartedOnTime(Instant jobStartTime) {
		final Instant startTime = getCurrentTime();
		final long timeDifference = ChronoUnit.MILLIS.between(jobStartTime, startTime);
		if (MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info(CLIENT_JOB_START_ON_TIME_LOG);
		} else {
			logger.info(CLIENT_JOB_START_DELAY_LOG, convertToRealTime(timeDifference));
		}
	}

	private void checkIfJobFinishedOnTime(Instant jobEndTime, Instant jobDeadline) {
		final Instant endTime = getCurrentTime();

		if (!jobDeadline.isBefore(endTime)) {
			final long timeDifference = ChronoUnit.MILLIS.between(endTime, jobEndTime);
			logger.info(CLIENT_JOB_FINISH_DELAY_BEFORE_DEADLINE_LOG, -1 * convertToRealTime(timeDifference));
		} else {
			final long deadlineDifference = ChronoUnit.MILLIS.between(endTime, jobDeadline);
			if (MAX_TIME_DIFFERENCE.isValidValue(deadlineDifference)) {
				logger.info(CLIENT_JOB_FINISH_ON_TIME_LOG);
			} else {
				logger.info(CLIENT_JOB_FINISH_DELAY_LOG, deadlineDifference);
			}
		}
	}

	private boolean isOriginalStatusUpdated(final JobStatusEnum status) {
		return switch (status) {
			case SCHEDULED, FINISHED -> checkIfAllPartsMatchStatus(status);
			case DELAYED -> myClientAgent.getCurrentJobStatus().equals(PROCESSED);
			case ON_BACK_UP -> List.of(IN_PROGRESS, PROCESSED).contains(myClientAgent.getCurrentJobStatus());
			case IN_PROGRESS -> checkIfAllPartsMatchStatus(IN_PROGRESS) || myClientAgent.getCurrentJobStatus()
					.equals(PROCESSED);
			case ON_HOLD -> true;
			default -> false;
		};
	}

	private void updateOriginalStatus(final JobStatusEnum status) {
		if(isOriginalStatusUpdated(status)) {
			myNode.updateJobStatus(status);
			myClientAgent.setCurrentJobStatus(status);
		}
	}

	private void shutdownAfterFinishedJob(String logMessage) {
		MDC.put(MDC_JOB_ID, myClientAgent.getMyJob().getJobId());
		logger.info(logMessage);
		myClientAgent.getGuiController().updateClientsCountByValue(-1);
		myClientAgent.getGuiController().updateFinishedJobsCountByValue(1);
		myClientAgent.doDelete();
	}

	private void handleJobSplitting(ACLMessage message) {
		SplitJob splitJob = readMessageContent(message, SplitJob.class);
		List<ClientJob> jobParts = splitJob.jobParts();

		logger.info(CLIENT_JOB_SPLIT_LOG);
		myClientAgent.split();
		myNode.informAboutSplitJob(jobParts);
		jobParts.forEach(jobPart ->
				myClientAgent.getJobParts().put(jobPart.getJobId(), new JobPart(jobPart, CREATED,
						myClientAgent.getSimulatedJobStart(), myClientAgent.getSimulatedJobEnd(),
						myClientAgent.getSimulatedDeadline())));
	}
}
