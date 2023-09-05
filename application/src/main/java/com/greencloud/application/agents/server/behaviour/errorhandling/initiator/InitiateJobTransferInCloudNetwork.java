package com.greencloud.application.agents.server.behaviour.errorhandling.initiator;

import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_FAILURE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_REFUSE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.CNA_JOB_TRANSFER_SUCCESSFUL_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.constants.MessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static com.greencloud.application.messages.constants.MessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareStringReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStateEnum;
import com.greencloud.commons.domain.resources.HardwareResources;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour sends a job transfer request to CNA
 */
public class InitiateJobTransferInCloudNetwork extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateJobTransferInCloudNetwork.class);

	private final ServerAgent myServerAgent;
	private final JobInstanceIdentifier jobToTransfer;
	private final ACLMessage greenSourceRequest;
	private final boolean handleForGreenSource;

	public InitiateJobTransferInCloudNetwork(final ServerAgent agent, final ACLMessage transferMessage,
			final ACLMessage greenSourceRequest, final JobInstanceIdentifier jobToTransfer) {
		super(agent, transferMessage);

		this.myServerAgent = agent;
		this.jobToTransfer = jobToTransfer;
		this.greenSourceRequest = greenSourceRequest;
		this.handleForGreenSource = nonNull(greenSourceRequest);
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent              agent executing the behaviour
	 * @param jobTransfer        transfer data which includes original job (i.e. job instance before its division)
	 * @param jobToTransfer      job instance that is to be transferred
	 * @param greenSourceRequest (optional) job transfer request received from Green Source
	 * @return InitiateJobTransferInCloudNetwork
	 */
	public static InitiateJobTransferInCloudNetwork create(final ServerAgent agent,
			final JobPowerShortageTransfer jobTransfer, final JobInstanceIdentifier jobToTransfer,
			final ACLMessage greenSourceRequest) {
		final AID cloudNetwork = agent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = prepareJobTransferRequest(jobTransfer, cloudNetwork);

		return new InitiateJobTransferInCloudNetwork(agent, transferMessage, greenSourceRequest, jobToTransfer);
	}

	/**
	 * Method handles the INFORM response retrieved from the Cloud Network Agent which indicates that the job transfer
	 * was successful.
	 * It updates the internal state of the server by finishing the transferred job instance as well as
	 * informs the Green Source which is executing the given job, that its execution should finish.
	 *
	 * @param inform response retrieved from Cloud Network
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(CNA_JOB_TRANSFER_SUCCESSFUL_LOG, job.getJobInstanceId());
			informGreenSourceUponJobFinish(job, null);
			updateServerStateUponJobFinish(job);
		}
	}

	/**
	 * Method handles the REFUSE response retrieved from the Cloud Network Agent which indicates that the
	 * job transfer won't be processed.
	 * It examines the cause of refusal and based on that initiates one of two actions:
	 *
	 * <p> 1) if job was not found in Cloud Network - job is being removed from the Server </p>
	 * <p> 2) if no servers are available for transfer - the status of the job is updated to either
	 * ON_HOLD/ON_HOLD_SOURCE_SHORTAGE or ON_BACK_UP_POWER depending on the current available power and the
	 * fluctuation "source " (i.e. whether it happened in the server or in the green source) </p>
	 *
	 * @param refuse response retrieved from Cloud Network
	 */
	@Override
	protected void handleRefuse(final ACLMessage refuse) {
		final String cause = refuse.getContent();
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());

			if (cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
				logger.info(CNA_JOB_TRANSFER_REFUSE_LOG, refuse.getSender().getLocalName(), job.getJobInstanceId());
				informGreenSourceUponJobFinish(job, cause);
				updateServerStateUponJobFinish(job);
			} else if (cause.equals(NO_SERVER_AVAILABLE_CAUSE_MESSAGE)) {
				informGreenSourceUponJobOnHold(job.getJobId(), cause);
				updateServerStateUponJobOnHold(job);
			}
		}
	}

	/**
	 * Method handles the FAILURE response retrieved from the Cloud Network Agent which indicates that the
	 * job transfer has failed in selected for transfer Server.
	 *
	 * @param failure response retrieved from Cloud Network
	 */
	@Override
	protected void handleFailure(final ACLMessage failure) {
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(CNA_JOB_TRANSFER_FAILURE_LOG, job.getJobInstanceId());

			informGreenSourceUponJobOnHold(job.getJobId(), failure.getContent());
			updateServerStateUponJobOnHold(job);
		}
	}

	private void informGreenSourceUponJobFinish(final ClientJob job, final String refuseCause) {
		final AID receiver = myServerAgent.getGreenSourceForJobMap().get(job.getJobId());
		myServerAgent.send(prepareJobFinishMessage(job, receiver));

		if (nonNull(refuseCause)) {
			if (handleForGreenSource) {
				myServerAgent.send(prepareStringReply(greenSourceRequest, refuseCause, REFUSE));
			}
		} else if (handleForGreenSource) {
			myServerAgent.send(prepareStringReply(greenSourceRequest, TRANSFER_SUCCESSFUL_MESSAGE, INFORM));
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
		myServerAgent.manage().updateGUI();
	}

	private void informGreenSourceUponJobOnHold(final String jobId, final String failureCause) {
		if (isNull(greenSourceRequest)) {
			final AID receiver = myServerAgent.getGreenSourceForJobMap().get(jobId);
			myServerAgent.send(prepareNetworkFailureInformation(jobToTransfer,
					INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL, receiver));
		} else {
			myServerAgent.send(prepareStringReply(greenSourceRequest, failureCause, REFUSE));
		}
	}

	private void updateServerStateUponJobOnHold(final ClientJob job) {
		final Triple<JobExecutionStateEnum, String, String> fieldsForUpdate = getFieldsForJobState(job);
		myServerAgent.manage().handleJobStateChange(fieldsForUpdate, job);
	}

	private Triple<JobExecutionStateEnum, String, String> getFieldsForJobState(final ClientJob job) {
		final HardwareResources availableResources = myServerAgent.resources()
				.getAvailableResources(job, jobToTransfer, null);

		if (!handleForGreenSource) {
			return new ImmutableTriple<>(EXECUTING_ON_HOLD, CNA_JOB_TRANSFER_PUT_ON_HOLD_LOG, ON_HOLD_JOB_ID);
		} else if (!availableResources.areSufficient(job.getEstimatedResources())) {
			return new ImmutableTriple<>
					(EXECUTING_ON_HOLD_SOURCE, CNA_JOB_TRANSFER_PUT_ON_HOLD_SOURCE_LOG, ON_HOLD_JOB_ID);
		}
		return new ImmutableTriple<>(EXECUTING_ON_BACK_UP, CNA_JOB_TRANSFER_PUT_ON_BACKUP_LOG, BACK_UP_POWER_JOB_ID);
	}
}
