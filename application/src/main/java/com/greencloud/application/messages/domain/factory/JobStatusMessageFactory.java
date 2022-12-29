package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SPLIT_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ANNOUNCED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CHANGE_JOB_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.util.Collections.singletonList;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ImmutableJobTimeFrames;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.application.domain.job.JobTimeFrames;
import com.greencloud.application.domain.job.SplitJob;
import com.greencloud.commons.job.ClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages passing job status
 */
public class JobStatusMessageFactory {

	/**
	 * Method prepares the message informing the Scheduler that a new client appeared
	 *
	 * @param schedulerAID agent identifier of the scheduler agent
	 * @param job          job that is to be announced
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobAnnouncementMessage(final AID schedulerAID, final ClientJob job) {
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		informationMessage.setProtocol(ANNOUNCED_JOB_PROTOCOL);
		informationMessage.addReceiver(schedulerAID);
		try {
			informationMessage.setContent(getMapper().writeValueAsString(job));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return informationMessage;
	}

	/**
	 * Method prepares the information message about the job execution status sent to the scheduler
	 *
	 * @param agent           Cloud Network sending the message
	 * @param jobStatusUpdate details regarding job status update
	 * @param conversationId  type of the message passed to scheduler
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForScheduler(final CloudNetworkAgent agent,
			final JobStatusUpdate jobStatusUpdate, final String conversationId) {
		return prepareJobStatusMessage(singletonList(agent.getScheduler()), jobStatusUpdate, conversationId);
	}

	/**
	 * Method prepares the information message about the job execution status sent to client with job instance and
	 * change time as message content
	 *
	 * @param job            job of interest
	 * @param conversationId type of the message passed for the client
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final ClientJob job, final String conversationId) {
		final JobStatusUpdate jobStatusUpdate = new JobStatusUpdate(mapToJobInstanceId(job), getCurrentTime());
		return prepareJobStatusMessage(singletonList(new AID(job.getClientIdentifier(), AID.ISGUID)), jobStatusUpdate,
				conversationId);
	}

	/**
	 * Method prepares the information message about the job execution status sent to client with job id as message
	 * content
	 *
	 * @param client          client to which the message is sent
	 * @param jobStatusUpdate job update information
	 * @param conversationId  type of the message passed for the client
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final String client,
			final JobStatusUpdate jobStatusUpdate, final String conversationId) {
		return prepareJobStatusMessage(singletonList(new AID(client, AID.ISGUID)), jobStatusUpdate, conversationId);
	}

	/**
	 * Method prepares the information message about the job split sent to client with split jobs as message content
	 *
	 * @param client   client to which the message is sent
	 * @param splitJob jobs created after split
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareSplitJobMessageForClient(final String client, final SplitJob splitJob) {
		return prepareJobStatusMessage(singletonList(new AID(client, AID.ISGUID)), splitJob, SPLIT_JOB_ID);
	}

	/**
	 * Method prepares the information message about the job split sent to client with jobId as message content
	 *
	 * @param job job of interest
	 * @return inform ACLMessage
	 */
	public static ACLMessage preparePostponeJobMessageForClient(final ClientJob job) {
		return prepareJobStatusMessage(singletonList(new AID(job.getClientIdentifier(), AID.ISGUID)), job.getJobId(),
				POSTPONED_JOB_ID);
	}

	/**
	 * Method prepares the information message sent to client containing adjusted job time frames
	 *
	 * @param client      client to which the message is sent
	 * @param adjustedJob job with adjusted time frames
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobAdjustmentMessage(final String client, final ClientJob adjustedJob) {
		final JobTimeFrames jobTimeFrames = ImmutableJobTimeFrames.builder()
				.newJobStart(adjustedJob.getStartTime())
				.newJobEnd(adjustedJob.getEndTime())
				.jobId(adjustedJob.getJobId())
				.build();
		return prepareJobStatusMessage(singletonList(new AID(client, AID.ISGUID)), jobTimeFrames, RE_SCHEDULED_JOB_ID);
	}

	/**
	 * Method prepares the message about the job changing its status
	 *
	 * @param jobInstanceId  unique job instance
	 * @param server         server that is sending the message
	 * @param conversationId conversation identifier informing about message type
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForCNA(final JobInstanceIdentifier jobInstanceId,
			final String conversationId, final ServerAgent server) {
		return prepareJobStatusMessage(
				singletonList(server.getOwnerCloudNetworkAgent()),
				new JobStatusUpdate(jobInstanceId, getCurrentTime()),
				conversationId);
	}

	/**
	 * Method prepares the information message about the job execution finish which is to be sent
	 * to list of receivers
	 *
	 * @param jobId        unique identifier of the kob of interest
	 * @param jobStartTime time when the job execution started
	 * @param receivers    list of AID addresses of the message receivers
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobFinishMessage(final String jobId, final Instant jobStartTime,
			final List<AID> receivers) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(jobId, jobStartTime);
		return prepareJobStatusMessage(receivers, new JobStatusUpdate(jobInstanceId, getCurrentTime()), FINISH_JOB_ID);
	}

	/**
	 * Method prepares the information message stating that the job execution has started
	 *
	 * @param jobId        unique identifier of the kob of interest
	 * @param jobStartTime time when the job execution started
	 * @param receivers    list of AID addresses of the message receivers
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStartedMessage(final String jobId, final Instant jobStartTime,
			final List<AID> receivers) {
		final JobInstanceIdentifier jobInstanceId = mapToJobInstanceId(jobId, jobStartTime);
		return prepareJobStatusMessage(receivers, new JobStatusUpdate(jobInstanceId, getCurrentTime()), STARTED_JOB_ID);
	}

	/**
	 * Method prepares the information message about finishing the power delivery by hand by the Green Source.
	 *
	 * @param jobInstanceId identifier of the job instance
	 * @param serverAddress server address
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareManualFinishMessageForServer(final JobInstanceIdentifier jobInstanceId,
			final AID serverAddress) {
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		informationMessage.setProtocol(MANUAL_JOB_FINISH_PROTOCOL);
		try {
			informationMessage.setContent(getMapper().writeValueAsString(jobInstanceId));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		informationMessage.addReceiver(serverAddress);
		return informationMessage;
	}

	/**
	 * Method prepares the message requesting the job start status from the server
	 *
	 * @param jobId    unique identifier of the kob of interest
	 * @param receiver server which will receive the message
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStartStatusRequestMessage(final String jobId, final AID receiver) {
		final ACLMessage requestMessage = new ACLMessage(REQUEST);
		requestMessage.setProtocol(JOB_START_STATUS_PROTOCOL);
		requestMessage.setContent(jobId);
		requestMessage.addReceiver(receiver);
		return requestMessage;
	}

	private static ACLMessage prepareJobStatusMessage(final List<AID> receivers, final Object content,
			final String conversationId) {
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		informationMessage.setProtocol(CHANGE_JOB_STATUS_PROTOCOL);
		if (content instanceof String stringContent) {
			informationMessage.setContent(stringContent);
		} else {
			try {
				informationMessage.setContent(getMapper().writeValueAsString(content));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		informationMessage.setConversationId(conversationId);
		receivers.forEach(informationMessage::addReceiver);
		return informationMessage;
	}
}
