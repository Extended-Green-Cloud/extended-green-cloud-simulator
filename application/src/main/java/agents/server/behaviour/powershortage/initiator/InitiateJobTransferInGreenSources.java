package agents.server.behaviour.powershortage.initiator;

import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_CHOSEN_GS_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_FAIL_BACK_UP_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_FAIL_NO_BACK_UP_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_NONE_AVAILABLE_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG;
import static agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.MessagingUtils.retrieveValidMessages;
import static messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static messages.domain.constants.powershortage.PowerShortageMessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;
import static utils.GUIUtils.displayMessageArrow;
import static utils.JobMapUtils.getJobByIdAndStartDate;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import agents.server.behaviour.powershortage.listener.ListenForSourceJobTransferConfirmation;
import common.mapper.JobMapper;
import domain.GreenSourceData;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import domain.job.PowerJob;
import domain.powershortage.PowerShortageJob;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviours sends the CFP to remaining green sources looking for job transfer and selects the one which will
 * handle the remaining job execution
 */
public class InitiateJobTransferInGreenSources extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobTransferInGreenSources.class);

	private final ServerAgent myServerAgent;
	private final String guid;
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
		this.guid = myAgent.getName();
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
			logger.info(GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG, guid);
			handleTransferFailure();
		} else if (proposals.isEmpty()) {
			logger.info(GS_TRANSFER_NONE_AVAILABLE_LOG, guid, jobToTransfer.getJobId());
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
		logger.info(GS_TRANSFER_CHOSEN_GS_LOG, guid, jobId, chosenOffer.getSender().getLocalName());

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
		final Job job = getJobByIdAndStartDate(myServerAgent.getServerJobs(), jobToTransferInstance);
		if (Objects.nonNull(job)) {
			final int availableBackUpPower = myServerAgent.manage()
					.getAvailableCapacity(jobToTransfer.getStartTime(), jobToTransfer.getEndTime(),
							jobToTransferInstance, BACK_UP_POWER);

			if (availableBackUpPower < jobToTransfer.getPower()) {
				logger.info(GS_TRANSFER_FAIL_NO_BACK_UP_LOG, guid, jobToTransfer.getJobId());
				myServerAgent.getServerJobs().replace(job, ON_HOLD_SOURCE_SHORTAGE);
			} else {
				logger.info(GS_TRANSFER_FAIL_BACK_UP_LOG, guid, jobToTransfer.getJobId());
				myServerAgent.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY);
			}
			myServerAgent.manage().updateServerGUI();
			displayMessageArrow(myServerAgent, greenSourceRequest.getSender());
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), NO_SOURCES_AVAILABLE_CAUSE_MESSAGE,
					ACLMessage.FAILURE));
		}
	}
}
