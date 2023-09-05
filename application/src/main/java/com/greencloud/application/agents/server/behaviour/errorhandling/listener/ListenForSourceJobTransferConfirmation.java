package com.greencloud.application.agents.server.behaviour.errorhandling.listener;

import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog.GS_TRANSFER_CONFIRMED_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog.GS_TRANSFER_JOB_FINISHED_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.templates.ErrorHandlingMessageTemplates.SOURCE_JOB_TRANSFER_CONFIRMATION_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.TRANSFER_EXPIRATION_TIME;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.constants.MessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareStringReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ON_HOLD_TRANSFER;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.MessageTemplate.MatchContent;
import static jade.lang.acl.MessageTemplate.and;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.handler.HandleSourceJobTransfer;
import com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.commons.domain.job.ClientJob;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

/**
 * Behaviour listens for the message from Green Source which confirms the power transfer
 */
public class ListenForSourceJobTransferConfirmation extends MsgReceiver {

	private static final Logger logger = getLogger(ListenForSourceJobTransferConfirmation.class);

	private final ServerAgent myServerAgent;
	private final JobDivided<ClientJob> newJobInstances;
	private final ACLMessage greenSourceRequest;
	private final Instant shortageStart;

	/**
	 * Behaviours constructor
	 *
	 * @param agent              server executing the behaviour
	 * @param newJobInstances    pair of job instances including previous job instance (first) and job to transfer
	 *                           instance (second)
	 * @param powerShortageTime  time when the power shortage starts
	 * @param greenSourceRequest original green source job transfer request
	 */
	public ListenForSourceJobTransferConfirmation(final ServerAgent agent,
			final JobDivided<ClientJob> newJobInstances, final Instant powerShortageTime,
			final ACLMessage greenSourceRequest) {
		super(agent, createListenerTemplate(newJobInstances.getSecondInstance()),
				currentTimeMillis() + TRANSFER_EXPIRATION_TIME, null, null);

		this.myServerAgent = agent;
		this.greenSourceRequest = greenSourceRequest;
		this.newJobInstances = newJobInstances;
		this.shortageStart = powerShortageTime;
	}

	private static MessageTemplate createListenerTemplate(final ClientJob job) {
		try {
			final String expectedContent = getMapper().writeValueAsString(mapToJobInstanceId(job));
			return and(MatchContent(expectedContent), SOURCE_JOB_TRANSFER_CONFIRMATION_TEMPLATE);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method listens for the confirmation message coming from Green Energy Source.
	 * It schedules the transfer execution and sends the response to Green Source which requested the transfer.
	 */
	@Override
	public void handleMessage(final ACLMessage msg) {
		if (nonNull(msg)) {
			final String jobId = newJobInstances.getSecondInstance().getJobId();

			MDC.put(MDC_JOB_ID, jobId);
			switch (msg.getPerformative()) {
				case INFORM -> handleJobTransfer(msg);
				default -> handleTransferFailure(jobId);
			}
		} else {
			block();
		}
	}

	private void handleJobTransfer(final ACLMessage inform) {
		final ClientJob job = getJobByInstanceId(newJobInstances.getSecondInstance().getJobInstanceId(),
				myServerAgent.getServerJobs());

		if (nonNull(job)) {
			logger.info(GS_TRANSFER_CONFIRMED_LOG, job.getJobId());
			myServerAgent.send(prepareReply(greenSourceRequest, TRANSFER_SUCCESSFUL_MESSAGE, INFORM));
			updateJobStatus(job);
			myAgent.addBehaviour(HandleSourceJobTransfer.create(myServerAgent, newJobInstances, inform.getSender()));
		} else {
			logger.info(GS_TRANSFER_JOB_FINISHED_LOG);
			myServerAgent.send(prepareStringReply(greenSourceRequest, JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
		}
	}

	private void handleTransferFailure(final String jobId) {
		logger.info(ErrorHandlingListenerLog.GS_TRANSFER_FAILED_LOG, jobId);
		myServerAgent.message()
				.passTransferRequestToCloudNetwork(mapToJobInstanceId(newJobInstances.getSecondInstance()),
						shortageStart, greenSourceRequest);
	}

	private void updateJobStatus(final ClientJob jobToExecute) {
		final boolean isJobRunning = myServerAgent.getServerJobs().get(jobToExecute).equals(ON_HOLD_TRANSFER);
		myServerAgent.getServerJobs().replace(jobToExecute, EXECUTING_ON_GREEN.getStatus(isJobRunning));
	}
}
