package agents.cloudnetwork.behaviour.powershortage.listener;

import static agents.cloudnetwork.behaviour.powershortage.listener.logs.PowerShortageCloudListenerLog.SERVER_TRANSFER_REQUEST_ASK_SERVERS_LOG;
import static agents.cloudnetwork.behaviour.powershortage.listener.logs.PowerShortageCloudListenerLog.SERVER_TRANSFER_REQUEST_JOB_NOT_FOUND_LOG;
import static agents.cloudnetwork.behaviour.powershortage.listener.logs.PowerShortageCloudListenerLog.SERVER_TRANSFER_REQUEST_NO_SERVERS_AVAILABLE_LOG;
import static agents.cloudnetwork.behaviour.powershortage.listener.templates.PowerShortageCloudMessageTemplates.SERVER_JOB_TRANSFER_REQUEST_TEMPLATE;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static messages.domain.constants.PowerShortageMessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_PROCESSING_MESSAGE;
import static messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;
import static messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static utils.GUIUtils.displayMessageArrow;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import agents.cloudnetwork.behaviour.powershortage.initiator.InitiateJobTransferRequest;
import domain.job.ClientJob;
import mapper.JobMapper;
import domain.job.JobInstanceIdentifier;
import domain.powershortage.PowerShortageJob;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour receives the request from the Server which has the power shortage and needs to perform the job transfer
 */
public class ListenForServerJobTransferRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForServerJobTransferRequest.class);

	private CloudNetworkAgent myCloudNetworkAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to agent of type Cloud Network Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = myAgent.getName();
	}

	/**
	 * Method listens for the messages coming from the Server requesting job transfer.
	 * It handles the information by announcing the job transfer request in network and looking for another server which may execute the job
	 */
	@Override
	public void action() {
		final ACLMessage transferRequest = myAgent.receive(SERVER_JOB_TRANSFER_REQUEST_TEMPLATE);
		if (Objects.nonNull(transferRequest)) {
			final PowerShortageJob powerShortageJob = readMessageContent(transferRequest, PowerShortageJob.class);
			final JobInstanceIdentifier jobInstance = powerShortageJob.getJobInstanceId();
			final Instant shortageStartTime = powerShortageJob.getPowerShortageStart();
			final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobInstance.getJobId());

			if (Objects.nonNull(job)) {
				myCloudNetworkAgent.send(
						prepareReply(transferRequest.createReply(), TRANSFER_PROCESSING_MESSAGE, ACLMessage.AGREE));
				final List<AID> remainingServers = getRemainingServers(transferRequest.getSender());

				if (!remainingServers.isEmpty()) {
					logger.info(SERVER_TRANSFER_REQUEST_ASK_SERVERS_LOG, guid, job.getJobId());
					askRemainingServersToTransferJob(remainingServers, job, shortageStartTime, transferRequest);
				} else {
					logger.info(SERVER_TRANSFER_REQUEST_NO_SERVERS_AVAILABLE_LOG, guid);
					replyWithFailedTransferForNoServers(transferRequest);
				}
			} else {
				logger.info(SERVER_TRANSFER_REQUEST_JOB_NOT_FOUND_LOG, guid, jobInstance.getJobId());
				final ACLMessage refuseMessage = prepareStringReply(transferRequest.createReply(),
						JOB_NOT_FOUND_CAUSE_MESSAGE, ACLMessage.REFUSE);
				myCloudNetworkAgent.send(refuseMessage);
			}
		} else {
			block();
		}
	}

	private void askRemainingServersToTransferJob(final List<AID> remainingServers, final ClientJob originalJob,
			final Instant shortageStartTime, final ACLMessage originalRequest) {
		final ClientJob jobToTransfer = prepareJobToTransfer(originalJob, shortageStartTime);
		final PowerShortageJob newPowerShortageJob = JobMapper.mapToPowerShortageJob(jobToTransfer, shortageStartTime);
		final ACLMessage cfp = createCallForProposal(jobToTransfer, remainingServers, CNA_JOB_CFP_PROTOCOL);

		displayMessageArrow(myCloudNetworkAgent, remainingServers);
		myAgent.addBehaviour(new InitiateJobTransferRequest(myAgent, cfp, originalRequest, newPowerShortageJob,
				jobToTransfer.getClientIdentifier()));
	}

	private void replyWithFailedTransferForNoServers(final ACLMessage originalRequest) {
		final ACLMessage reply = prepareReply(originalRequest.createReply(), NO_SERVER_AVAILABLE_CAUSE_MESSAGE,
				ACLMessage.FAILURE);
		displayMessageArrow(myCloudNetworkAgent, originalRequest.getSender());
		myCloudNetworkAgent.send(reply);
	}

	private ClientJob prepareJobToTransfer(final ClientJob job, final Instant shortageStartTime) {
		final Instant startTime = job.getStartTime().isAfter(shortageStartTime) ?
				job.getStartTime() :
				shortageStartTime;
		return JobMapper.mapToJobNewStartTime(job, startTime);
	}

	private List<AID> getRemainingServers(final AID serverSender) {
		return myCloudNetworkAgent.getOwnedServers().stream()
				.filter(server -> !server.equals(serverSender)).toList();
	}
}
