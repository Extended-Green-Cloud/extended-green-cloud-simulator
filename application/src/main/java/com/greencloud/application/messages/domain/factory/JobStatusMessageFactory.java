package com.greencloud.application.messages.domain.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages passing job status
 */
public class JobStatusMessageFactory {

	/**
	 * Method prepares the information message about the job execution start which is to be sent
	 * to the client
	 *
	 * @param clientId client global name
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobStatusMessageForClient(final String clientId, final String protocol) {
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		informationMessage.setProtocol(protocol);
		informationMessage.setContent(protocol);
		informationMessage.addReceiver(new AID(clientId, AID.ISGUID));
		return informationMessage;
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
	public static ACLMessage prepareFinishMessage(final String jobId, final Instant jobStartTime,
			final List<AID> receivers) {
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder().jobId(jobId)
				.startTime(jobStartTime).build();
		try {
			informationMessage.setContent(JsonMapper.getMapper().writeValueAsString(jobInstanceId));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		informationMessage.setProtocol(MessageProtocolConstants.FINISH_JOB_PROTOCOL);
		receivers.forEach(informationMessage::addReceiver);
		return informationMessage;
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
		informationMessage.setProtocol(MessageProtocolConstants.MANUAL_JOB_FINISH_PROTOCOL);
		try {
			informationMessage.setContent(JsonMapper.getMapper().writeValueAsString(jobInstanceId));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		informationMessage.addReceiver(serverAddress);
		return informationMessage;
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
		final ACLMessage informationMessage = new ACLMessage(INFORM);
		informationMessage.setProtocol(MessageProtocolConstants.STARTED_JOB_PROTOCOL);
		final JobInstanceIdentifier jobInstanceId = ImmutableJobInstanceIdentifier.builder().jobId(jobId)
				.startTime(jobStartTime).build();
		try {
			informationMessage.setContent(JsonMapper.getMapper().writeValueAsString(jobInstanceId));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		receivers.forEach(informationMessage::addReceiver);
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
		requestMessage.setProtocol(MessageProtocolConstants.JOB_START_STATUS_PROTOCOL);
		requestMessage.setContent(jobId);
		requestMessage.addReceiver(receiver);
		return requestMessage;
	}
}
