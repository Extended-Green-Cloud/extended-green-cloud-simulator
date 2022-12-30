package com.greencloud.application.agents.server.behaviour.powershortage.listener;

import static com.greencloud.application.agents.server.behaviour.powershortage.listener.logs.PowerShortageServerListenerLog.GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.listener.logs.PowerShortageServerListenerLog.GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDate;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static jade.lang.acl.ACLMessage.REFUSE;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.powershortage.handler.HandleServerPowerShortage;
import com.greencloud.application.agents.server.behaviour.powershortage.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.application.agents.server.behaviour.powershortage.initiator.InitiateJobTransferInGreenSources;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.templates.PowerShortageServerMessageTemplates;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;
import com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory;
import com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.PowerJob;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the job transfer request coming from Green Source
 */
public class ListenForSourceJobTransferRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForSourceJobTransferRequest.class);
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
	 * Method listens for the REQUEST messages coming from the Green Source informing about power shortage and requesting the
	 * job transfer.
	 * It sends the CFP to remaining Green Sources asking for job transfer.
	 * If there are no available green sources, it passes the transfer request to the parent Cloud Network.
	 */
	@Override
	public void action() {
		final ACLMessage transferRequest = myAgent.receive(
				PowerShortageServerMessageTemplates.SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE);

		if (Objects.nonNull(transferRequest)) {
			final PowerShortageJob affectedJob = readMessageContent(transferRequest);

			if (Objects.nonNull(affectedJob)) {
				final ClientJob originalJob = getJobByIdAndStartDate(affectedJob.getJobInstanceId(),
						myServerAgent.getServerJobs());

				if (Objects.nonNull(originalJob)) {
					final PowerJob powerJob = createJobTransferInstance(affectedJob, originalJob);
					final List<AID> remainingGreenSources = getRemainingGreenSources(transferRequest.getSender());
					myAgent.send(prepareReply(transferRequest.createReply(),
							TRANSFER_SUCCESSFUL_MESSAGE, ACLMessage.AGREE));

					MDC.put(MDC_JOB_ID, powerJob.getJobId());
					if (!remainingGreenSources.isEmpty()) {
						logger.info(GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG, powerJob.getJobId());
						askForTransferInRemainingGS(remainingGreenSources, powerJob,
								affectedJob.getPowerShortageStart(), transferRequest);
					} else {
						logger.info(GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG);
						passTransferRequestToCNA(affectedJob, powerJob, transferRequest);
					}
					schedulePowerShortageHandling(affectedJob, transferRequest);
				} else {
					myAgent.send(prepareReply(transferRequest.createReply(), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
				}
			}
		} else {
			block();
		}
	}

	private PowerJob createJobTransferInstance(final PowerShortageJob jobTransfer, final ClientJob originalJob) {
		final Instant startTime = originalJob.getStartTime().isAfter(jobTransfer.getPowerShortageStart()) ?
				originalJob.getStartTime() :
				jobTransfer.getPowerShortageStart();
		return JobMapper.mapToPowerJob(originalJob, startTime);
	}

	private void askForTransferInRemainingGS(final List<AID> remainingGreenSources, final PowerJob powerJob,
			final Instant shortageStartTime, final ACLMessage transferRequest) {
		final ACLMessage cfp = CallForProposalMessageFactory.createCallForProposal(powerJob,
				remainingGreenSources, MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL);

		myAgent.addBehaviour(
				new InitiateJobTransferInGreenSources(myAgent, cfp, transferRequest, powerJob, shortageStartTime));
	}

	private void passTransferRequestToCNA(final PowerShortageJob affectedJob, final PowerJob powerJob,
			final ACLMessage gsTransferRequest) {
		final PowerShortageJob jobToTransfer = JobMapper.mapToPowerShortageJob(powerJob,
				affectedJob.getPowerShortageStart());
		final AID cloudNetwork = myServerAgent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = PowerShortageMessageFactory.preparePowerShortageTransferRequest(affectedJob,
				cloudNetwork);

		myServerAgent.addBehaviour(
				new InitiateJobTransferInCloudNetwork(myServerAgent, transferMessage, gsTransferRequest,
						jobToTransfer));
	}

	private void schedulePowerShortageHandling(final PowerShortageJob jobTransfer, final ACLMessage transferRequest) {
		final ClientJob job = getJobByIdAndStartDate(jobTransfer.getJobInstanceId(), myServerAgent.getServerJobs());
		if (Objects.nonNull(job)) {
			myServerAgent.manage().divideJobForPowerShortage(job, jobTransfer.getPowerShortageStart());
			myServerAgent.addBehaviour(HandleServerPowerShortage.createFor(Collections.singletonList(job),
					jobTransfer.getPowerShortageStart(), myServerAgent, null));
		} else {
			myAgent.send(prepareReply(transferRequest.createReply(), jobTransfer.getJobInstanceId(),
					REFUSE));
		}
	}

	private PowerShortageJob readMessageContent(final ACLMessage message) {
		try {
			return JsonMapper.getMapper().readValue(message.getContent(), PowerShortageJob.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<AID> getRemainingGreenSources(final AID greenSourceSender) {
		return myServerAgent.manage().getOwnedActiveGreenSources().stream()
				.filter(greenSource -> !greenSource.equals(greenSourceSender)).toList();
	}
}
