package agents.server.behaviour.powershortage.listener;

import static agents.server.behaviour.powershortage.listener.logs.PowerShortageServerListenerLog.GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG;
import static agents.server.behaviour.powershortage.listener.logs.PowerShortageServerListenerLog.GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG;
import static agents.server.behaviour.powershortage.listener.templates.PowerShortageServerMessageTemplates.SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE;
import static mapper.JsonMapper.getMapper;
import static messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;
import static utils.GUIUtils.displayMessageArrow;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import agents.server.behaviour.powershortage.handler.HandleServerPowerShortage;
import agents.server.behaviour.powershortage.initiator.InitiateJobTransferInCloudNetwork;
import agents.server.behaviour.powershortage.initiator.InitiateJobTransferInGreenSources;
import domain.job.ClientJob;
import mapper.JobMapper;
import domain.job.PowerJob;
import domain.powershortage.PowerShortageJob;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import messages.domain.factory.CallForProposalMessageFactory;

/**
 * Behaviour listens for the job transfer request coming from Green Source
 */
public class ListenForSourceJobTransferRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForSourceJobTransferRequest.class);
	private ServerAgent myServerAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method listens for the REQUEST messages coming from the Green Source informing about power shortage and requesting the
	 * job transfer.
	 * It sends the CFP to remaining Green Sources asking for job transfer.
	 * If there are no available green sources, it passes the transfer request to the parent Cloud Network.
	 */
	@Override
	public void action() {
		final ACLMessage transferRequest = myAgent.receive(SOURCE_JOB_TRANSFER_REQUEST_TEMPLATE);

		if (Objects.nonNull(transferRequest)) {
			final PowerShortageJob affectedJob = readMessageContent(transferRequest);

			if (Objects.nonNull(affectedJob)) {
				final ClientJob originalJob = myServerAgent.manage().getJobByIdAndStartDate(affectedJob.getJobInstanceId());

				if (Objects.nonNull(originalJob)) {
					final PowerJob powerJob = createJobTransferInstance(affectedJob, originalJob);
					final List<AID> remainingGreenSources = getRemainingGreenSources(transferRequest.getSender());
					myAgent.send(prepareReply(transferRequest.createReply(), TRANSFER_SUCCESSFUL_MESSAGE,
							ACLMessage.AGREE));

					if (!remainingGreenSources.isEmpty()) {
						logger.info(GS_TRANSFER_REQUEST_ASK_OTHER_GS_LOG, myAgent.getName(), powerJob.getJobId());
						askForTransferInRemainingGS(remainingGreenSources, powerJob,
								affectedJob.getPowerShortageStart(), transferRequest);
					} else {
						logger.info(GS_TRANSFER_REQUEST_NO_GS_AVAILABLE_LOG, guid);
						passTransferRequestToCNA(affectedJob, powerJob, transferRequest);
					}
					schedulePowerShortageHandling(affectedJob, transferRequest);
				} else {
					myAgent.send(prepareReply(transferRequest.createReply(), JOB_NOT_FOUND_CAUSE_MESSAGE,
							ACLMessage.REFUSE));
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
				remainingGreenSources, SERVER_JOB_CFP_PROTOCOL);

		displayMessageArrow(myServerAgent, remainingGreenSources);
		myAgent.addBehaviour(
				new InitiateJobTransferInGreenSources(myAgent, cfp, transferRequest, powerJob, shortageStartTime));
	}

	private void passTransferRequestToCNA(final PowerShortageJob affectedJob, final PowerJob powerJob,
			final ACLMessage gsTransferRequest) {
		final PowerShortageJob jobToTransfer = JobMapper.mapToPowerShortageJob(powerJob,
				affectedJob.getPowerShortageStart());
		final AID cloudNetwork = myServerAgent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(affectedJob, cloudNetwork);

		displayMessageArrow(myServerAgent, cloudNetwork);
		myServerAgent.addBehaviour(
				new InitiateJobTransferInCloudNetwork(myServerAgent, transferMessage, gsTransferRequest,
						jobToTransfer));
	}

	private void schedulePowerShortageHandling(final PowerShortageJob jobTransfer, final ACLMessage transferRequest) {
		final ClientJob job = myServerAgent.manage().getJobByIdAndStartDate(jobTransfer.getJobInstanceId());
		if (Objects.nonNull(job)) {
			myServerAgent.manage().divideJobForPowerShortage(job, jobTransfer.getPowerShortageStart());
			myServerAgent.addBehaviour(HandleServerPowerShortage.createFor(Collections.singletonList(job),
					jobTransfer.getPowerShortageStart(), myServerAgent, null));
		} else {
			myAgent.send(prepareReply(transferRequest.createReply(), jobTransfer.getJobInstanceId(),
					ACLMessage.REFUSE));
		}
	}

	private PowerShortageJob readMessageContent(final ACLMessage message) {
		try {
			return getMapper().readValue(message.getContent(), PowerShortageJob.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<AID> getRemainingGreenSources(final AID greenSourceSender) {
		return myServerAgent.getOwnedGreenSources().stream()
				.filter(greenSource -> !greenSource.equals(greenSourceSender)).toList();
	}
}
