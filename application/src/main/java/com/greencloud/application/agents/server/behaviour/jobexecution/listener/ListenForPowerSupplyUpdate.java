package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_INFORM_CNA_TRANSFER_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FAILURE_INFORM_CNA_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_FAILURE_INFORM_CNA_TRANSFER_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.SUPPLY_UPDATE_JOB_FINISHED_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.POWER_SUPPLY_UPDATE_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_MESSAGE_NUMBER;
import static com.greencloud.application.messages.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.CONFIRMED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareJobTransferUpdateMessageForCNA;
import static com.greencloud.application.utils.GuiUtils.announceBookedJob;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PLANNED_JOB_STATUSES;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobStart;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for energy supply update messages coming from Green Source
 */
public class ListenForPowerSupplyUpdate extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForPowerSupplyUpdate.class);

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
	 * Method listens for the messages coming from Green Source with updates regarding energy supply state.
	 * It considers both new jobs and job transfers.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(POWER_SUPPLY_UPDATE_TEMPLATE, MAX_MESSAGE_NUMBER);

		if (nonNull(messages)) {
			messages.forEach(message -> {
				final JobInstanceIdentifier jobInstance = readMessageContent(message, JobInstanceIdentifier.class);
				final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), myServerAgent.getServerJobs());

				if (nonNull(job)) {
					MDC.put(MDC_JOB_ID, job.getJobId());
					switch (message.getPerformative()) {
						case INFORM -> processInform(message, job, jobInstance);
						case FAILURE -> processFailure(message, job, jobInstance);
					}
				} else {
					logger.info(SUPPLY_UPDATE_JOB_FINISHED_LOG, jobInstance.getJobId());
				}
			});
		} else {
			block();
		}
	}

	private void processInform(final ACLMessage msg, final ClientJob job, final JobInstanceIdentifier jobInstance) {
		logger.info(SUPPLY_CONFIRMATION_JOB_SCHEDULING_LOG, jobInstance.getJobId());

		myServerAgent.getServerJobs().replace(job, ACCEPTED);
		myServerAgent.manage().updateClientNumberGUI();

		switch (msg.getProtocol()) {
			case SERVER_JOB_CFP_PROTOCOL -> handleNewJobConfirmation(jobInstance, job);
			case POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL -> handleTransferredJobConfirmation(jobInstance, job);
		}

	}

	private void processFailure(final ACLMessage msg, final ClientJob job, final JobInstanceIdentifier jobInstance) {
		final String jobId = job.getJobId();

		if (isJobUnique(jobId, myServerAgent.getServerJobs())) {
			myServerAgent.getGreenSourceForJobMap().remove(jobId);
		}
		myServerAgent.getServerJobs().remove(job);

		myServerAgent.manage().incrementJobCounter(jobInstance, FAILED);
		myServerAgent.manage().updateGUI();

		switch (msg.getProtocol()) {
			case FAILED_JOB_PROTOCOL -> handleNewJobFailure(jobInstance);
			case FAILED_TRANSFER_PROTOCOL -> handleTransferJobFailure(jobInstance);
		}
	}

	private void handleNewJobConfirmation(final JobInstanceIdentifier jobInstance, final ClientJob job) {
		logger.info(SUPPLY_CONFIRMATION_JOB_ANNOUNCEMENT_LOG, jobInstance.getJobId());
		announceBookedJob(myServerAgent);
		myServerAgent.message().informCNAAboutStatusChange(jobInstance, CONFIRMED_JOB_ID);
		myAgent.addBehaviour(HandleJobStart.createFor(myServerAgent, job, true, true));
	}

	private void handleTransferredJobConfirmation(final JobInstanceIdentifier jobInstance, final ClientJob job) {
		logger.info(SUPPLY_CONFIRMATION_INFORM_CNA_TRANSFER_LOG, jobInstance.getJobId());
		myServerAgent.send(prepareJobTransferUpdateMessageForCNA(jobInstance, CONFIRMED_TRANSFER_PROTOCOL,
				myServerAgent));
		myAgent.addBehaviour(HandleJobStart.createFor(myServerAgent, job,
				PLANNED_JOB_STATUSES.contains(myServerAgent.getServerJobs().get(job)), true));
	}

	private void handleNewJobFailure(final JobInstanceIdentifier jobInstance) {
		logger.info(SUPPLY_FAILURE_INFORM_CNA_LOG, jobInstance.getJobId());
		myServerAgent.message().informCNAAboutStatusChange(jobInstance, FAILED_JOB_ID);
	}

	private void handleTransferJobFailure(final JobInstanceIdentifier jobInstance) {
		logger.info(SUPPLY_FAILURE_INFORM_CNA_TRANSFER_LOG, jobInstance.getJobId());
		myServerAgent.send(prepareJobTransferUpdateMessageForCNA(jobInstance, FAILED_TRANSFER_PROTOCOL, myServerAgent));
	}

}
