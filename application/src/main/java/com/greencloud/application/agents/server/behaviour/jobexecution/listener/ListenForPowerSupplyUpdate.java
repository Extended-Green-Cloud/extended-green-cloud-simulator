package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_INFORM_CNA_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_INFORM_CNA_TRANSFER_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_FINISHED_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FAILURE_INFORM_CNA_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FAILURE_INFORM_CNA_TRANSFER_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FINISHED_MANUALLY_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CONFIRMED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobTransferUpdateMessageForCNA;
import static com.greencloud.application.utils.GUIUtils.announceBookedJob;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDate;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PLANNED_JOB_STATUSES;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobStart;
import com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ExecutionJobStatusEnum;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for power supply update messages coming from Green Source
 */
public class ListenForPowerSupplyUpdate extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForPowerSupplyUpdate.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method listens for the messages coming from Green Source with updates regarding power supply.
	 * It handles two types of messages:
	 *
	 * - messages confirming that given green source will provide power necessary to supply the job (either new job
	 * or transferred one)
	 * - messages informing that the job execution was finished manually as the message about job finish did not come
	 * on time
	 */
	@Override
	public void action() {
		final ACLMessage inform = myAgent.receive(JobHandlingMessageTemplates.POWER_SUPPLY_UPDATE_TEMPLATE);

		if (Objects.nonNull(inform)) {
			if (inform.getProtocol().equals(MANUAL_JOB_FINISH_PROTOCOL)) {
				handlePowerSupplyManualFinishMessage(inform);
			} else {
				handlePowerResponseMessage(inform);
			}
		} else {
			block();
		}
	}

	private void handlePowerSupplyManualFinishMessage(final ACLMessage inform) {
		final ClientJob job = retrieveJobFromMessage(inform);
		final ExecutionJobStatusEnum statusEnum = isNull(job) ?
				null :
				myServerAgent.getServerJobs().getOrDefault(job, null);

		if (nonNull(statusEnum) && statusEnum.equals(ExecutionJobStatusEnum.IN_PROGRESS)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.debug(SUPPLY_FINISHED_MANUALLY_LOG, job.getClientIdentifier(), job.getClientIdentifier());
			myServerAgent.manage().finishJobExecution(job, true);
		}
	}

	private void handlePowerResponseMessage(final ACLMessage msg) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(msg, JobInstanceIdentifier.class);
		final String messageType = msg.getProtocol();
		final String jobId = jobInstanceId.getJobId();

		if (msg.getPerformative() == ACLMessage.INFORM) {
			if (Objects.nonNull(getJobByIdAndStartDate(jobInstanceId, myServerAgent.getServerJobs()))) {
				if (messageType.equals(SERVER_JOB_CFP_PROTOCOL)) {
					MDC.put(MDC_JOB_ID, jobId);
					logger.info(SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG, jobId);
					announceBookedJob(myServerAgent);
				}
				confirmJobAcceptance(jobInstanceId, messageType.equals(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL));
				scheduleJobExecution(jobInstanceId, messageType);
			}
		} else {
			failJobAcceptance(messageType, jobInstanceId, msg);
		}
	}

	private void scheduleJobExecution(final JobInstanceIdentifier jobInstanceId, final String messageType) {
		final ClientJob job = getJobByIdAndStartDate(jobInstanceId, myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG, jobInstanceId.getJobId());
			final boolean informCNAStart = messageType.equals(SERVER_JOB_CFP_PROTOCOL) ||
					PLANNED_JOB_STATUSES.contains(myServerAgent.getServerJobs().get(job));
			myAgent.addBehaviour(HandleJobStart.createFor(myServerAgent, job, informCNAStart, true));
		} else {
			logger.info(SUPPLY_CONFIRMATION_JOB_FINISHED_LOG, jobInstanceId.getJobId());
		}
	}

	private void confirmJobAcceptance(final JobInstanceIdentifier jobInstanceId, final boolean isTransferred) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());

		myServerAgent.getServerJobs().replace(getJobByIdAndStartDate(jobInstanceId, myServerAgent.getServerJobs()),
				ExecutionJobStatusEnum.ACCEPTED);
		myServerAgent.manage().updateClientNumberGUI();

		if (!isTransferred) {
			myServerAgent.manage().informCNAAboutStatusChange(jobInstanceId, CONFIRMED_JOB_ID);
			logger.info(SUPPLY_CONFIRMATION_INFORM_CNA_LOG, jobInstanceId.getJobId());
		} else {
			myServerAgent.send(
					prepareJobTransferUpdateMessageForCNA(jobInstanceId, CONFIRMED_TRANSFER_PROTOCOL, myServerAgent));
			logger.info(SUPPLY_CONFIRMATION_INFORM_CNA_TRANSFER_LOG, jobInstanceId.getJobId());
		}
	}

	private void failJobAcceptance(final String protocol, final JobInstanceIdentifier jobInstanceId,
			final ACLMessage message) {
		final boolean isTransferred = protocol.equals(FAILED_TRANSFER_PROTOCOL);
		final ClientJob job = retrieveJobFromMessage(message);
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());

		if (Objects.nonNull(job)) {
			if (isJobUnique(job.getJobId(), myServerAgent.getServerJobs())) {
				myServerAgent.getGreenSourceForJobMap().remove(job.getJobId());
			}
			myServerAgent.getServerJobs().remove(job);
		}
		myServerAgent.manage().incrementJobCounter(jobInstanceId, FAILED);
		myServerAgent.manage().updateServerGUI();

		if (!isTransferred) {
			myServerAgent.manage().informCNAAboutStatusChange(jobInstanceId, FAILED_JOB_ID);
			logger.info(SUPPLY_FAILURE_INFORM_CNA_LOG, jobInstanceId.getJobId());
		} else {
			myServerAgent.send(
					prepareJobTransferUpdateMessageForCNA(jobInstanceId, FAILED_TRANSFER_PROTOCOL, myServerAgent));
			logger.info(SUPPLY_FAILURE_INFORM_CNA_TRANSFER_LOG, jobInstanceId.getJobId());
		}
	}

	private ClientJob retrieveJobFromMessage(final ACLMessage msg) {
		try {
			final JobInstanceIdentifier identifier = readMessageContent(msg, JobInstanceIdentifier.class);
			return getJobByIdAndStartDate(identifier, myServerAgent.getServerJobs());
		} catch (IncorrectMessageContentException e) {
			final String jobId = msg.getContent();
			return getJobById(jobId, myServerAgent.getServerJobs());
		}
	}

}
