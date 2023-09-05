package com.greencloud.application.messages.factory;

import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.NETWORK_ERROR_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.message.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages for communicating the network errors
 */
public class NetworkErrorMessageFactory {

	/**
	 * Method prepares the message containing the request regarding job transfer
	 *
	 * @param powerShortageJob content of the message consisting of the job to transfer and power shortage time
	 * @param receiver         receivers of the message
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage prepareJobTransferRequest(final JobPowerShortageTransfer powerShortageJob,
			final AID receiver) {
		return MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withObjectContent(powerShortageJob)
				.withMessageProtocol(NETWORK_ERROR_ALERT_PROTOCOL)
				.withReceivers(receiver)
				.build();
	}

	/**
	 * Method prepares the message containing the request regarding job green power re-supply
	 *
	 * @param job      job affected by source power shortage to be supplied again using green power
	 * @param receiver receiver of the message
	 * @return REQUEST ACLMessage
	 */
	public static ACLMessage prepareGreenPowerSupplyRequest(final ClientJob job, final AID receiver) {
		return MessageBuilder.builder()
				.withPerformative(REQUEST)
				.withObjectContent(mapToJobInstanceId(job))
				.withMessageProtocol(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL)
				.withReceivers(receiver)
				.build();
	}

	/**
	 * Method prepares the message about the job transfer update that is sent to scheduler
	 *
	 * @param jobInstanceId unique job instance
	 * @param server        server that is sending the message
	 * @param protocol      protocol used in transfer messages
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareJobTransferUpdateMessageForCNA(final JobInstanceIdentifier jobInstanceId,
			final String protocol, final ServerAgent server) {
		final int performative = protocol.equals(FAILED_TRANSFER_PROTOCOL) ? FAILURE : INFORM;
		return MessageBuilder.builder()
				.withObjectContent(jobInstanceId)
				.withPerformative(performative)
				.withReceivers(server.getOwnerCloudNetworkAgent())
				.withMessageProtocol(protocol)
				.build();
	}

	/**
	 * Method prepares the message passing the job affected by some error with provided protocol
	 *
	 * @param messageContent message content
	 * @param receivers      address of a receiver agents
	 * @param protocol       message protocol
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareNetworkFailureInformation(final Object messageContent, final String protocol,
			final AID... receivers) {
		return MessageBuilder.builder()
				.withPerformative(INFORM)
				.withMessageProtocol(protocol)
				.withObjectContent(messageContent)
				.withReceivers(receivers)
				.build();
	}
}
