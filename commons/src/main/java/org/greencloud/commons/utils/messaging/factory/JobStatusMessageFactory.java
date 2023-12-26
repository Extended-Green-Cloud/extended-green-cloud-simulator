package org.greencloud.commons.utils.messaging.factory;

import static jade.core.AID.ISGUID;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.util.Objects;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithTimeFrames;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithTimeFrames;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import org.greencloud.commons.utils.messaging.constants.MessageConversationConstants;
import org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages passing job status
 */
public class JobStatusMessageFactory {

	/**
	 * Method prepares the information message about the job execution status sent to client with job instance and
	 * change time as message content
	 *
	 * @param job            job of interest
	 * @param conversationId type of the message passed for the client
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final ClientJob job, final String conversationId,
			final Integer ruleSet) {
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(mapClientJobToJobInstanceId(job))
				.changeTime(getCurrentTime())
				.build();
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, ruleSet, clientAID);
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
			final JobWithStatus jobStatusUpdate, final String conversationId, final Integer ruleSet) {
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, ruleSet, clientAID);
	}

	/**
	 * Method prepares the job postponing message that is sent to the client with jobId as message content
	 *
	 * @param job job of interest
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage preparePostponeJobMessageForClient(final ClientJob job, final Integer ruleSet) {
		final AID clientAID = new AID(job.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(job.getClientAddress());
		return prepareJobStatusMessage(job.getJobId(), MessageConversationConstants.POSTPONED_JOB_ID, ruleSet,
				clientAID);
	}

	/**
	 * Method prepares the information message sent to client containing adjusted job time frames
	 *
	 * @param adjustedJob job with adjusted time frames
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobAdjustmentMessage(final ClientJob adjustedJob, final Integer ruleSet) {
		final JobWithTimeFrames jobTimeFrames = new ImmutableJobWithTimeFrames(adjustedJob.getStartTime(),
				adjustedJob.getEndTime(), adjustedJob.getJobId());
		final AID clientAID = new AID(adjustedJob.getClientIdentifier(), ISGUID);
		clientAID.addAddresses(adjustedJob.getClientAddress());
		return prepareJobStatusMessage(jobTimeFrames, MessageConversationConstants.RE_SCHEDULED_JOB_ID, ruleSet,
				clientAID);
	}

	/**
	 * Method prepares the information message about the job execution status sent to the scheduler
	 *
	 * @param agentProps      properties of Cloud Netowrk Agent
	 * @param jobStatusUpdate details regarding job status update
	 * @param conversationId  type of the message passed to scheduler
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForScheduler(final RegionalManagerAgentProps agentProps,
			final JobWithStatus jobStatusUpdate, final String conversationId, final Integer ruleSet) {
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, ruleSet, agentProps.getScheduler());
	}

	/**
	 * Method prepares the message about the job changing its status that is sent to the Regional Manager Agent
	 *
	 * @param jobInstanceId  unique job instance
	 * @param agentProps     properties of Server Agent
	 * @param conversationId conversation identifier informing about message type
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForRMA(final JobInstanceIdentifier jobInstanceId,
			final String conversationId, final ServerAgentProps agentProps, final Integer ruleSet) {
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(jobInstanceId)
				.changeTime(getCurrentTime())
				.build();
		final AID rma = agentProps.getOwnerRegionalManagerAgent();

		if (Objects.equals(conversationId, MessageConversationConstants.FAILED_JOB_ID)) {
			return MessageBuilder.builder(ruleSet)
					.withPerformative(FAILURE)
					.withMessageProtocol(MessageProtocolConstants.FAILED_JOB_PROTOCOL)
					.withObjectContent(jobStatusUpdate)
					.withReceivers(rma)
					.build();
		}
		return prepareJobStatusMessage(jobStatusUpdate, conversationId, ruleSet, rma);
	}

	/**
	 * Method prepares the message informing the Scheduler that a new client appeared in the system
	 *
	 * @param scheduler agent identifier of the scheduler agent
	 * @param job       job that is to be announced
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobAnnouncementMessage(final AID scheduler, final ClientJob job,
			final Integer ruleSet) {
		return MessageBuilder.builder(ruleSet)
				.withPerformative(INFORM)
				.withMessageProtocol(MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL)
				.withReceivers(scheduler)
				.withObjectContent(job)
				.build();
	}

	/**
	 * Method prepares default message that informs about changes in job status
	 *
	 * @param content        content of the message
	 * @param conversationId message type
	 * @param receivers      message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessage(final Object content, final String conversationId,
			final Integer ruleSet, final AID... receivers) {
		final MessageBuilder messageBasis = MessageBuilder.builder(ruleSet)
				.withPerformative(INFORM)
				.withMessageProtocol(MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL)
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

	/**
	 * Method prepares the information message stating that the job execution has started
	 *
	 * @param job       job of interest
	 * @param receivers list of AID addresses of the message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobStartedMessage(final ClientJob job, final Integer ruleSet,
			final AID... receivers) {
		final JobInstanceIdentifier jobInstanceId = mapClientJobToJobInstanceId(job);
		final JobWithStatus jobWithStatus = ImmutableJobWithStatus.builder()
				.jobInstance(jobInstanceId)
				.changeTime(getCurrentTime())
				.build();
		return prepareJobStatusMessage(jobWithStatus, STARTED_JOB_ID, ruleSet, receivers);
	}

	/**
	 * Method prepares the information message about the job execution finish which is to be sent
	 * to the list of receivers
	 *
	 * @param job       job of interest
	 * @param receivers list of AID addresses of the message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobFinishMessage(final ClientJob job, final Integer ruleSet,
			final AID... receivers) {
		final JobInstanceIdentifier jobInstanceId = mapClientJobToJobInstanceId(job);
		final JobWithStatus jobWithStatus = ImmutableJobWithStatus.builder()
				.jobInstance(jobInstanceId)
				.changeTime(getCurrentTime())
				.build();
		return prepareJobStatusMessage(jobWithStatus, FINISH_JOB_ID, ruleSet, receivers);
	}

	/**
	 * Method prepares the information message about the job execution finish which is to be sent to Regional Manager Agent
	 *
	 * @param job       job of interest
	 * @param receivers list of AID addresses of the message receivers
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobFinishMessageForRMA(final ClientJob job, final Integer ruleSet,
			final Double price, final AID... receivers) {
		final JobInstanceIdentifier jobInstanceId = mapClientJobToJobInstanceId(job);
		final JobWithStatus jobInstanceWithPrice = ImmutableJobWithStatus.builder()
				.jobInstance(jobInstanceId)
				.changeTime(getCurrentTime())
				.priceForJob(price)
				.build();

		return prepareJobStatusMessage(jobInstanceWithPrice, FINISH_JOB_ID, ruleSet, receivers);
	}

	/**
	 * Method prepares the information message about finishing the power delivery by hand by the Green Source
	 *
	 * @param jobInstanceId identifier of the job instance
	 * @param serverAddress server address
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareManualFinishMessageForServer(final JobInstanceIdentifier jobInstanceId,
			final AID serverAddress, final Integer ruleSet) {
		return MessageBuilder.builder(ruleSet)
				.withPerformative(INFORM)
				.withMessageProtocol(MANUAL_JOB_FINISH_PROTOCOL)
				.withObjectContent(jobInstanceId)
				.withReceivers(serverAddress)
				.build();
	}
}
