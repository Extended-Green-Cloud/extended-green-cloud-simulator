package com.greencloud.application.agents.server.behaviour.errorhandling.listener;

import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog.GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.logs.ErrorHandlingListenerLog.GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.listener.templates.ErrorHandlingMessageTemplates.SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_MESSAGE_NUMBER;
import static com.greencloud.application.messages.constants.MessageContentConstants.DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE;
import static com.greencloud.application.messages.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareStringReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.handler.HandleInternalServerError;
import com.greencloud.application.agents.server.behaviour.errorhandling.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.application.agents.server.behaviour.errorhandling.initiator.InitiateJobTransferInGreenSources;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the job transfer request coming from Green Source
 */
public class ListenForSourceJobTransferRequest extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForSourceJobTransferRequest.class);
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
	 * Method listens for the messages coming from the Green Source informing about power shortage and requesting the
	 * job transfer.
	 * It sends the CFP to remaining Green Sources asking for job transfer.
	 * If there are no available green sources, it passes the transfer request to the parent Cloud Network.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE, MAX_MESSAGE_NUMBER);

		if (nonNull(messages)) {
			messages.forEach(this::processTransferMessage);
		} else {
			block();
		}
	}

	private void processTransferMessage(final ACLMessage message) {
		final JobPowerShortageTransfer transfer = readMessageContent(message, JobPowerShortageTransfer.class);
		final ClientJob job = getJobByInstanceId(transfer.getOriginalJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());

			if (!job.getEndTime().isAfter(getCurrentTime())) {
				myAgent.send(prepareStringReply(message, DELAYED_JOB_ALREADY_FINISHED_CAUSE_MESSAGE, REFUSE));
			} else {
				final JobDivided<ClientJob> newJobInstances = schedulePowerShortageHandling(transfer, job);
				final List<AID> greenSources = myServerAgent.manage().getRemainingAgents(message.getSender(),
						myServerAgent.manage().getOwnedActiveGreenSources());

				if (!greenSources.isEmpty()) {
					logger.info(GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG, job.getJobId());
					myAgent.addBehaviour(InitiateJobTransferInGreenSources.create(myServerAgent, newJobInstances,
							greenSources, message, transfer.getPowerShortageStart()));
				} else {
					logger.info(GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG);
					passTransferRequestToCNA(transfer, transfer.getSecondJobInstanceId(), message);
				}
			}
		} else {
			myAgent.send(prepareStringReply(message, JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
		}
	}

	private void passTransferRequestToCNA(final JobPowerShortageTransfer affectedJob,
			final JobInstanceIdentifier jobInstance, final ACLMessage transferRequest) {
		myServerAgent.addBehaviour(InitiateJobTransferInCloudNetwork.create(myServerAgent, affectedJob, jobInstance,
				transferRequest));
	}

	private JobDivided<ClientJob> schedulePowerShortageHandling(final JobPowerShortageTransfer jobTransfer,
			final ClientJob job) {
		myServerAgent.addBehaviour(HandleInternalServerError.createFor(singletonList(job),
				jobTransfer.getPowerShortageStart(), myServerAgent, false));
		return myServerAgent.manage().divideJobForTransfer(jobTransfer, job);
	}
}
