package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.NETWORK_ERROR_ALERT_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.jrba.utils.messages.MessageBuilder;

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
			final AID receiver, final Integer ruleSet) {
		return MessageBuilder.builder(ruleSet, REQUEST)
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
	public static ACLMessage prepareGreenPowerSupplyRequest(final ClientJob job, final AID receiver,
			final Integer ruleSet) {
		return MessageBuilder.builder(ruleSet, REQUEST)
				.withObjectContent(mapClientJobToJobInstanceId(job))
				.withMessageProtocol(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL)
				.withReceivers(receiver)
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
			final Integer ruleSet, final AID... receivers) {
		return MessageBuilder.builder(ruleSet, INFORM)
				.withMessageProtocol(protocol)
				.withObjectContent(messageContent)
				.withReceivers(receivers)
				.build();
	}
}
