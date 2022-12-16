package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.commons.job.ClientJob;

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
		final ACLMessage message = new ACLMessage(REQUEST);
		try {
			message.setContent(getMapper().writeValueAsString(powerShortageJob));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		message.setProtocol(POWER_SHORTAGE_ALERT_PROTOCOL);
		message.addReceiver(receiver);
		return message;
	}

	/**
	 * Method prepares the message containing the request regarding job green power re-supply
	 *
	 * @param job      job affected by source power shortage to be supplied again using green power
	 * @param receiver receiver of the message
	 * @return request ACLMessage
	 */
	public static ACLMessage prepareGreenPowerSupplyRequest(final ClientJob job, final AID receiver) {
		final ACLMessage message = new ACLMessage(REQUEST);
		try {
			message.setContent(getMapper().writeValueAsString(mapToJobInstanceId(job)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		message.setProtocol(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL);
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
		final ACLMessage message = new ACLMessage(INFORM);
		try {
			message.setContent(getMapper().writeValueAsString(messageContent));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		message.setProtocol(protocol);
		message.addReceiver(receiver);
		return message;
	}
}
