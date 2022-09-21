package com.greencloud.application.agents.server.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_CHOSEN_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_FAIL_BACK_UP_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_FAIL_NO_BACK_UP_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_NONE_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG;
import static com.greencloud.application.agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static com.greencloud.application.messages.MessagingUtils.rejectJobOffers;
import static com.greencloud.application.messages.MessagingUtils.retrieveProposals;
import static com.greencloud.application.messages.MessagingUtils.retrieveValidMessages;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.powershortage.listener.ListenForSourceJobTransferConfirmation;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Behaviours sends the CFP to remaining green sources looking for job transfer and selects the one which will
 * handle the remaining job execution
 */
public class InitiateJobTransferInGreenSources extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobTransferInGreenSources.class);

	private final ServerAgent myServerAgent;
	private final PowerJob jobToTransfer;
	private final JobInstanceIdentifier jobToTransferInstance;
	private final Instant powerShortageStart;
	private final ACLMessage greenSourceRequest;

	/**
	 * Behaviour constructor
	 *
	 * @param agent              agent which executes the behaviour
	 * @param powerRequest       call for proposal sent to GSAs containing the details regarding job to be transfered
	 * @param greenSourceRequest green source power transfer request
	 * @param jobToTransfer      job to be transferred
	 * @param powerShortageStart time when the power shortage starts
	 */
	public InitiateJobTransferInGreenSources(final Agent agent,
			final ACLMessage powerRequest,
			final ACLMessage greenSourceRequest,
			final PowerJob jobToTransfer,
			final Instant powerShortageStart) {
		super(agent, powerRequest);
		this.myServerAgent = (ServerAgent) myAgent;
		this.jobToTransfer = jobToTransfer;
		this.greenSourceRequest = greenSourceRequest;
		this.powerShortageStart = powerShortageStart;
		this.jobToTransferInstance = JobMapper.mapToJobInstanceId(jobToTransfer);
	}

	/**
	 * Method handles Green Source Agent responses. It analyzes received proposals and selects one GSA for power job transfer.
	 * If no green source is available, it passes the information about the need of the job transfer to the parent Cloud Network
	 *
	 * @param responses   retrieved responses from Green Source Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen green source (not used)
	 */
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		if (responses.isEmpty()) {
			logger.info(GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG);
			handleTransferFailure();
		} else if (proposals.isEmpty()) {
			logger.info(GS_TRANSFER_NONE_AVAILABLE_LOG, jobToTransfer.getJobId());
			forwardRequestToCloudNetwork();
		} else {
			final List<ACLMessage> validProposals = retrieveValidMessages(proposals, GreenSourceData.class);
			if (!validProposals.isEmpty()) {
				final ACLMessage chosenOffer = myServerAgent.chooseGreenSourceToExecuteJob(validProposals);
				initiateTransferForGreenSource(jobToTransfer.getJobId(), chosenOffer);
				rejectJobOffers(myServerAgent, jobToTransferInstance, chosenOffer, proposals);
			} else {
				handleInvalidProposals(proposals);
			}
		}
	}

	private void forwardRequestToCloudNetwork() {
		final PowerShortageJob jobTransfer = JobMapper.mapToPowerShortageJob(jobToTransfer, powerShortageStart);
		final AID cloudNetwork = myServerAgent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(jobTransfer, cloudNetwork);
		displayMessageArrow(myServerAgent, cloudNetwork);
		myServerAgent.addBehaviour(
				new InitiateJobTransferInCloudNetwork(myServerAgent, transferMessage, greenSourceRequest,
						jobTransfer));
	}

	private void initiateTransferForGreenSource(final String jobId, final ACLMessage chosenOffer) {
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(GS_TRANSFER_CHOSEN_GS_LOG, jobId, chosenOffer.getSender().getLocalName());

		displayMessageArrow(myServerAgent, chosenOffer.getSender());
		myServerAgent.addBehaviour(
				new ListenForSourceJobTransferConfirmation(myServerAgent, jobToTransferInstance,
						greenSourceRequest));
		myAgent.send(ReplyMessageFactory.prepareAcceptReplyWithProtocol(chosenOffer.createReply(),
				jobToTransferInstance, POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL));
	}

	private void handleInvalidProposals(final List<ACLMessage> proposals) {
		handleTransferFailure();
		rejectJobOffers(myServerAgent, jobToTransferInstance, null, proposals);
	}

	private void handleTransferFailure() {
		final Job job = myServerAgent.manage().getJobByIdAndStartDate(jobToTransferInstance);
		if (Objects.nonNull(job)) {
			final int availableBackUpPower = myServerAgent.manage()
					.getAvailableCapacity(jobToTransfer.getStartTime(), jobToTransfer.getEndTime(),
							jobToTransferInstance, BACK_UP_POWER);

			MDC.put(MDC_JOB_ID, jobToTransfer.getJobId());
			if (availableBackUpPower < jobToTransfer.getPower()) {
				logger.info(GS_TRANSFER_FAIL_NO_BACK_UP_LOG, jobToTransfer.getJobId());
				myServerAgent.getServerJobs().replace(job, ON_HOLD_SOURCE_SHORTAGE);
			} else {
				logger.info(GS_TRANSFER_FAIL_BACK_UP_LOG, jobToTransfer.getJobId());
				myServerAgent.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY);
			}
			myServerAgent.manage().updateServerGUI();
			displayMessageArrow(myServerAgent, greenSourceRequest.getSender());
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), NO_SOURCES_AVAILABLE_CAUSE_MESSAGE,
					ACLMessage.FAILURE));
		}
	}
}
