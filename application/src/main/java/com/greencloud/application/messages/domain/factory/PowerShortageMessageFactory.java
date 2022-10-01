package com.greencloud.application.messages.domain.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.messages.domain.constants.MessageProtocolConstants;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages for communicating the power shortage
 */
public class PowerShortageMessageFactory {

	/**
	 * Method prepares the message containing the request regarding job transfer
	 *
	 * @param powerShortageJob content of the message consisting of the job to transfer and power shortage time
	 * @param receiver         receivers of the message
	 * @return request ACLMessage
	 */
	public static ACLMessage preparePowerShortageTransferRequest(final PowerShortageJob powerShortageJob,
			final AID receiver) {
		final ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		try {
			message.setContent(JsonMapper.getMapper().writeValueAsString(powerShortageJob));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		message.setProtocol(MessageProtocolConstants.POWER_SHORTAGE_ALERT_PROTOCOL);
		message.addReceiver(receiver);
		return message;
	}

	/**
	 * Method prepares the message passing the job affected by the power shortage with provided protocol
	 *
	 * @param messageContent message content
	 * @param receiver       address of a receiver agent
	 * @param protocol       message protocol
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareJobPowerShortageInformation(final Object messageContent,
			final AID receiver,
			final String protocol) {
		final ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		try {
			message.setContent(JsonMapper.getMapper().writeValueAsString(messageContent));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		message.setProtocol(protocol);
		message.addReceiver(receiver);
		return message;
	}

	/**
	 * Method prepares the message informing about the finish of the shortage in power for given agent
	 *
	 * @param jobInstanceId unique identifier of the job instance
	 * @param receiver      message receiver
	 * @return inform ACLMessage
	 */
	public static ACLMessage preparePowerShortageFinishInformation(final JobInstanceIdentifier jobInstanceId,
			final AID receiver) {
		return prepareJobPowerShortageInformation(jobInstanceId, receiver, MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL);
	}
}
