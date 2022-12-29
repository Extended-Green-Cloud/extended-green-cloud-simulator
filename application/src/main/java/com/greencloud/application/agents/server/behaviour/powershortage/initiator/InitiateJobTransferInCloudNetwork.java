package com.greencloud.application.agents.server.behaviour.powershortage.initiator;

import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PROCESSING_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_REFUSE_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.initiator.logs.PowerShortageServerInitiatorLog.CNA_JOB_TRANSFER_SUCCESSFUL_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDate;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.BACK_UP_POWER_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_PLANNED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_SOURCE_SHORTAGE_PLANNED;
import static com.greencloud.commons.job.JobResultType.FINISH;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.commons.job.ClientJob;

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
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobInstanceId().getJobId());
		logger.info(CNA_JOB_TRANSFER_PROCESSING_LOG, myServerAgent.getOwnerCloudNetworkAgent().getLocalName(),
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
		MDC.put(MDC_JOB_ID, jobToTransfer.getJobInstanceId().getJobId());
		logger.info(CNA_JOB_TRANSFER_REFUSE_LOG, myServerAgent.getOwnerCloudNetworkAgent().getLocalName(),
				jobToTransfer.getJobInstanceId().getJobId());
		final ClientJob job = getJobByIdAndStartDate(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());
		if (Objects.nonNull(job)) {
			final String cause = refuse.getContent();
			informGreenSourceUponJobFinish(job, cause);
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
		final ClientJob job = getJobByIdAndStartDate(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());
		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(CNA_JOB_TRANSFER_SUCCESSFUL_LOG, jobToTransfer.getJobInstanceId().getJobId());
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
		final ClientJob job = getJobByIdAndStartDate(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());
		if (nonNull(job)) {
			final String jobId = jobToTransfer.getJobInstanceId().getJobId();
			final String cause = readMessageContent(failure, String.class);
			informGreenSourceUponJobOnHold(jobId, cause);
			updateServerStateUponJobOnHold(job);
		}
	}

	private void informGreenSourceUponJobFinish(final ClientJob job, final String refuseCause) {
		if (isNull(greenSourceRequest) || nonNull(refuseCause)) {
			final List<AID> receivers = List.of(myServerAgent.getGreenSourceForJobMap().get(job.getJobId()));
			final ACLMessage finishJobMessage = prepareJobFinishMessage(job.getJobId(), job.getStartTime(), receivers);

			myServerAgent.send(finishJobMessage);

			if (nonNull(greenSourceRequest)) {
				myServerAgent.send(prepareReply(greenSourceRequest.createReply(), refuseCause, FAILURE));
			}
		} else {
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), TRANSFER_SUCCESSFUL_MESSAGE,
					INFORM));
		}
	}

	private void updateServerStateUponJobFinish(final ClientJob job) {
		if (isJobStarted(job, myServerAgent.getServerJobs())) {
			myServerAgent.manage().incrementJobCounter(mapToJobInstanceId(job), FINISH);
		}
		if (isJobUnique(job.getJobId(), myServerAgent.getServerJobs())) {
			myServerAgent.getGreenSourceForJobMap().remove(job.getJobId());
		}
		myServerAgent.getServerJobs().remove(job);
		myServerAgent.manage().updateServerGUI();
	}

	private void informGreenSourceUponJobOnHold(final String jobId, final String failureCause) {
		if (isNull(greenSourceRequest)) {
			final AID receiver = myServerAgent.getGreenSourceForJobMap().get(jobId);
			myServerAgent.send(prepareJobPowerShortageInformation(jobToTransfer, receiver,
					SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL));
		} else {
			myServerAgent.send(prepareReply(greenSourceRequest.createReply(), failureCause, FAILURE));
		}
	}

	private void updateServerStateUponJobOnHold(final ClientJob job) {
		final String jobId = job.getJobId();
		final int availableBackUpPower =
				myServerAgent.manage().getAvailableCapacity(job.getStartTime(), job.getEndTime(),
						jobToTransfer.getJobInstanceId(), BACK_UP_POWER_STATUSES);
		final boolean hasStarted = isJobStarted(job, myServerAgent.getServerJobs());

		MDC.put(MDC_JOB_ID, jobId);
		if (isNull(greenSourceRequest)) {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG, jobId);
			myServerAgent.getServerJobs().replace(job, hasStarted ? ON_HOLD : ON_HOLD_PLANNED);

			if (hasStarted) {
				myServerAgent.manage()
						.informCNAAboutStatusChange(mapToJobInstanceId(job), ON_HOLD_JOB_ID);
			}
		} else if (availableBackUpPower <= job.getPower()) {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG, jobId);
			myServerAgent.getServerJobs()
					.replace(job, hasStarted ? ON_HOLD_SOURCE_SHORTAGE : ON_HOLD_SOURCE_SHORTAGE_PLANNED);

			if (hasStarted) {
				myServerAgent.manage()
						.informCNAAboutStatusChange(mapToJobInstanceId(job), ON_HOLD_JOB_ID);
			}
		} else {
			logger.info(CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG, jobId);
			myServerAgent.getServerJobs()
					.replace(job, hasStarted ? IN_PROGRESS_BACKUP_ENERGY : IN_PROGRESS_BACKUP_ENERGY_PLANNED);

			if (hasStarted) {
				myServerAgent.manage()
						.informCNAAboutStatusChange(mapToJobInstanceId(job), BACK_UP_POWER_JOB_ID);
			}
		}
		myServerAgent.manage().updateServerGUI();
	}
}
