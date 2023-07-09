package com.greencloud.application.messages.factory;

import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_IN_CLOUD_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.SPLIT_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_IN_CLOUD_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.CANCEL_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static jade.core.AID.ISGUID;
import static jade.lang.acl.ACLMessage.CANCEL;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;

import java.util.Objects;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobStatusUpdate;
import com.greencloud.application.domain.job.ImmutableJobTimeFrames;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobParts;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.application.domain.job.JobTimeFrames;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages passing job status
 */
public class JobStatusMessageFactory {

	/**
	 * Method prepares the message informing the Scheduler that a new client appeared in the system
	 *
	 * @param scheduler agent identifier of the scheduler agent
	 * @param job       job that is to be announced
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobAnnouncementMessage(final AID scheduler, final ClientJob job) {
		return MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(ANNOUNCED_JOB_PROTOCOL)
				.withReceivers(scheduler)
				.withObjectContent(job)
				.build();
	}

	/**
	 * Method prepares the message requesting from the underlying agents the job cancellation
	 *
	 * @param jobId     identifier of original job
	 * @param receivers receivers of the message
	 * @return CANCEL ACLMessage
	 */
	public static ACLMessage prepareJobCancellationMessage(final String jobId, final AID... receivers) {
		return MessageBuilder.builder()
				.withPerformative(CANCEL)
				.withMessageProtocol(CANCEL_JOB_PROTOCOL)
				.withStringContent(jobId)
				.withReceivers(receivers)
				.build();
	}

	/**
	 * Method prepares the information message about the job execution status sent to the scheduler
	 *
	 * @param agent           Cloud Network sending the message
	 * @param jobStatusUpdate details regarding job status update
	 * @param conversationId  type of the message passed to scheduler
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForScheduler(final CloudNetworkAgent agent,
			final JobStatusUpdate jobStatusUpdate, final String conversationId) {
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, agent.getScheduler());
	}

	/**
	 * Method prepares the information message about the job execution status sent to client with job instance and
	 * change time as message content
	 *
	 * @param job            job of interest
	 * @param conversationId type of the message passed for the client
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final ClientJob job, final String conversationId) {
		final JobStatusUpdate jobStatusUpdate = new ImmutableJobStatusUpdate(mapToJobInstanceId(job), getCurrentTime());
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, clientAID);
	}

	/**
	 * Method prepares the information message about the job execution status sent to client with job id as message
	 * content
	 *
	 * @param job             client's job
	 * @param jobStatusUpdate job update information
	 * @param conversationId  type of the message passed for the client
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final ClientJob job,
			final JobStatusUpdate jobStatusUpdate, final String conversationId) {
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, clientAID);
	}

	/**
	 * Method prepares the information message about the job split sent to client with split jobs as message content
	 *
	 * @param job      client's job
	 * @param splitJob jobs created after split
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareSplitJobMessageForClient(final ClientJob job, final JobParts splitJob) {
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(splitJob, SPLIT_JOB_ID, clientAID);
	}

	/**
	 * Method prepares the job postponing message that is sent to the client with jobId as message content
	 *
	 * @param job job of interest
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage preparePostponeJobMessageForClient(final ClientJob job) {
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(job.getJobId(), POSTPONED_JOB_ID, clientAID);
	}

	/**
	 * Method prepares the information message sent to client containing adjusted job time frames
	 *
	 * @param adjustedJob job with adjusted time frames
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobAdjustmentMessage(final ClientJob adjustedJob) {
		final JobTimeFrames jobTimeFrames = new ImmutableJobTimeFrames(adjustedJob.getStartTime(),
				adjustedJob.getEndTime(), adjustedJob.getJobId());
		final AID clientAID = new AID(adjustedJob.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(adjustedJob.getClientAddress());
		return prepareJobStatusMessage(jobTimeFrames, RE_SCHEDULED_JOB_ID, clientAID);
	}

	/**
	 * Method prepares the message about the job changing its status that is sent to the Cloud Network Agent
	 *
	 * @param jobInstanceId  unique job instance
	 * @param server         server that is sending the message
	 * @param conversationId conversation identifier informing about message type
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForCNA(final JobInstanceIdentifier jobInstanceId,
			final String conversationId, final ServerAgent server) {
		final JobStatusUpdate jobStatusUpdate = new ImmutableJobStatusUpdate(jobInstanceId, getCurrentTime());
		final AID cna = server.getOwnerCloudNetworkAgent();

		if (Objects.equals(conversationId, FAILED_JOB_ID)) {
			return MessageBuilder.builder()
					.withPerformative(FAILURE)
					.withMessageProtocol(FAILED_JOB_PROTOCOL)
					.withObjectContent(jobStatusUpdate)
					.withReceivers(cna)
					.build();
		}
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, cna);
	}

	/**
	 * Method prepares the information message about the job execution finish which is to be sent
	 * to the list of receivers
	 *
	 * @param job       job of interest
	 * @param receivers list of AID addresses of the message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobFinishMessage(final ClientJob job, final AID... receivers) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);
		return prepareJobStatusMessage(new ImmutableJobStatusUpdate(jobInstanceId, getCurrentTime()), FINISH_JOB_ID,
				receivers);
	}

	/**
	 * Method prepares the information message stating that the job execution has started
	 *
	 * @param job       job of interest
	 * @param receivers list of AID addresses of the message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStartedMessage(final ClientJob job, final AID... receivers) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);
		return prepareJobStatusMessage(new ImmutableJobStatusUpdate(jobInstanceId, getCurrentTime()), STARTED_JOB_ID,
				receivers);
	}

	/**
	 * Method prepares the information message about the job execution finish in cloud which is to be sent
	 * to the Client
	 *
	 * @param job job of interest
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobFinishInCloudMessage(final ClientJob job) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(new ImmutableJobStatusUpdate(jobInstanceId, getCurrentTime()),
				FINISH_IN_CLOUD_JOB_ID, clientAID);
	}

	/**
	 * Method prepares the information message stating that the job execution has started in cloud
	 *
	 * @param job job of interest
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStartedInCloudMessage(final ClientJob job) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(job);
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(new ImmutableJobStatusUpdate(jobInstanceId, getCurrentTime()),
				STARTED_IN_CLOUD_JOB_ID, clientAID);
	}

	/**
	 * Method prepares the information message about finishing the power delivery by hand by the Green Source
	 *
	 * @param jobInstanceId identifier of the job instance
	 * @param serverAddress server address
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareManualFinishMessageForServer(final JobInstanceIdentifier jobInstanceId,
			final AID serverAddress) {
		return MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(MANUAL_JOB_FINISH_PROTOCOL)
				.withObjectContent(jobInstanceId)
				.withReceivers(serverAddress)
				.build();
	}

	private static ACLMessage prepareJobStatusMessage(final Object content, final String conversationId,
			final AID... receivers) {
		final MessageBuilder messageBasis = MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(CHANGE_JOB_STATUS_PROTOCOL)
				.withConversationId(conversationId)
				.withGeneratedReplyWith()
				.withReceivers(receivers);

		if (content instanceof String stringContent) {
			messageBasis.withStringContent(stringContent);
		} else {
			messageBasis.withObjectContent(content);
		}

		return messageBasis.build();
	}
}
