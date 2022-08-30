package agents.server.behaviour.powershortage.initiator;

import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PROCESSING_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_REFUSE_LOG;
import static agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_SUCCESSFUL_LOG;
import static agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static utils.GUIUtils.displayMessageArrow;
import static utils.TimeUtils.getCurrentTime;
import static domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static domain.job.JobStatusEnum.ON_HOLD;
import static domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL;
import static messages.domain.constants.powershortage.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static messages.domain.factory.JobStatusMessageFactory.prepareFinishMessage;
import static messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.Job;
import domain.powershortage.PowerShortageJob;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour sends a job transfer request to CNA
 */
public class InitiateJobTransferInCloudNetwork extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobTransferInCloudNetwork.class);

	private final ServerAgent myServerAgent;
	private final PowerShortageJob jobToTransfer;
	private final ACLMessage greenSourceRequest;
	private final String guid;

	/**
	 * Behaviours constructor
	 *
	 * @param agent              server executing the behaviour
	 * @param transferMessage    message with the transfer request sent to CNA
	 * @param greenSourceRequest message received from the green source asking for job transfer (optional)
	 * @param jobToTransfer      job instance that is to be transferred
	 */
	public InitiateJobTransferInCloudNetwork(final ServerAgent agent,
			final ACLMessage transferMessage,
			final ACLMessage greenSourceRequest,
			final PowerShortageJob jobToTransfer) {
		super(agent, transferMessage);
		this.myServerAgent = agent;
		this.guid = myServerAgent.getLocalName();
		this.jobToTransfer = jobToTransfer;
		this.greenSourceRequest = greenSourceRequest;
	}

	/**
	 * Method handles the AGREE response retrieved from the Cloud Network Agent indicating that
	 * the job transfer request will be processed.
	 * It logs the appropriate information
	 *
	 * @param agree response retrieved from Cloud Network
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		logger.info(CNA_JOB_TRANSFER_PROCESSING_LOG,
				guid, myServerAgent.getOwnerCloudNetworkAgent().getLocalName(),
				jobToTransfer.getJobInstanceId().getJobId());
	}

	/**
	 * Method handles the REFUSE response retrieved from the Cloud Network Agent indicating that
	 * the job transfer won't be processed.
	 * It passes the information to Green Source and updates internal state.
	 *
	 * @param refuse response retrieved from Cloud Network
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		logger.info(CNA_JOB_TRANSFER_REFUSE_LOG,
				guid, myServerAgent.getOwnerCloudNetworkAgent().getLocalName(),
				jobToTransfer.getJobInstanceId().getJobId());
		final Job job = myServerAgent.manage().getJobByIdAndStartDate(jobToTransfer.getJobInstanceId());
		if (Objects.nonNull(job)) {
			informGreenSourceUponJobFinish(job, refuse.getContent());
			updateServerStateUponJobFinish(job);
		}
	}

	/**
	 * Method handles the INFORM response retrieved from the Cloud Network Agent which indicates that the job transfer was successful.
	 * It updates the internal state of the server by finishing the transferred job instance as well as informs the Green Source
	 * which is executing the given job, that its execution should finish.
	 *
	 * @param inform response retrieved from Cloud Network
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final Job job = myServerAgent.manage().getJobByIdAndStartDate(jobToTransfer.getJobInstanceId());
		if (nonNull(job)) {
			logger.info(CNA_JOB_TRANSFER_SUCCESSFUL_LOG, guid, jobToTransfer.getJobInstanceId().getJobId());
			informGreenSourceUponJobFinish(job, null);
			updateServerStateUponJobFinish(job);
		}
	}

	/**
	 * Method handles the FAILURE response retrieved from the Cloud Network Agent which indicates that the job transfer was unsuccessful.
	 * It updates the status of the job instance to either ON_HOLD/ON_HOLD_SOURCE_SHORTAGE or
	 * ON_BACK_UP_POWER depending on the current available power as well as the power shortage "source"
	 * (whether it happened in the server or in the green source)
	 *
	 * @param failure response retrieved from Cloud Network
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		final Job job = myServerAgent.manage().getJobByIdAndStartDate(jobToTransfer.getJobInstanceId());
		if (nonNull(job)) {
			final String jobId = jobToTransfer.getJobInstanceId().getJobId();
			informGreenSourceUponJobOnHold(jobId, failure.getContent());
			updateServerStateUponJobOnHold(job);
		}
	}

	private void informGreenSourceUponJobFinish(final Job job, final String refuseCause) {
		if (isNull(greenSourceRequest) || nonNull(refuseCause)) {
			final List<AID> receivers = List.of(myServerAgent.getGreenSourceForJobMap().get(job.getJobId()));
			final ACLMessage finishJobMessage = prepareFinishMessage(job.getJobId(), job.getStartTime(), receivers);
			displayMessageArrow(myServerAgent, receivers);
			myServerAgent.send(finishJobMessage);

			if (nonNull(greenSourceRequest)) {
				myServerAgent.send(prepareReply(greenSourceRequest.createReply(), refuseCause, ACLMessage.FAILURE));
			}
		} else {
			displayMessageArrow(myServerAgent, greenSourceRequest.getSender());
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), TRANSFER_SUCCESSFUL_MESSAGE,
					ACLMessage.INFORM));
		}
	}

	private void updateServerStateUponJobFinish(final Job job) {
		if (job.getStartTime().isBefore(getCurrentTime())) {
			myServerAgent.manage().incrementFinishedJobs(job.getJobId());
		}
		if (myServerAgent.manage().isJobUnique(job.getJobId())) {
			myServerAgent.getGreenSourceForJobMap().remove(job.getJobId());
		}
		myServerAgent.getServerJobs().remove(job);
		myServerAgent.manage().updateServerGUI();
	}

	private void informGreenSourceUponJobOnHold(final String jobId, final String failureCause) {
		if (isNull(greenSourceRequest)) {
			final AID receiver = myServerAgent.getGreenSourceForJobMap().get(jobId);
			displayMessageArrow(myServerAgent, receiver);
			myServerAgent.send(prepareJobPowerShortageInformation(jobToTransfer, receiver,
					SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL));
		} else {
			displayMessageArrow(myServerAgent, greenSourceRequest.getSender());
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), failureCause, ACLMessage.FAILURE));
		}
	}

	private void updateServerStateUponJobOnHold(final Job job) {
		final String jobId = job.getJobId();
		final int availableBackUpPower =
				myServerAgent.manage().getAvailableCapacity(job.getStartTime(), job.getEndTime(),
						jobToTransfer.getJobInstanceId(), BACK_UP_POWER);

		if (isNull(greenSourceRequest)) {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG, guid, jobId);
			myServerAgent.getServerJobs().replace(job, ON_HOLD);
		} else if (availableBackUpPower <= job.getPower()) {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG, guid, jobId);
			myServerAgent.getServerJobs().replace(job, ON_HOLD_SOURCE_SHORTAGE);
		} else {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG, guid, jobId);
			myServerAgent.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY);
		}
		myServerAgent.manage().updateServerGUI();
	}
}
