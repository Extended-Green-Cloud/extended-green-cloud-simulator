package com.greencloud.application.messages.domain.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.mapper.JsonMapper;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating messages for power checking
 */
public class PowerCheckMessageFactory {

	/**
	 * Method prepares the information message about the job execution start which is to be sent
	 * to the client
	 *
	 * @param greenEnergyAgent agent sending the request
	 * @param requestData      data passed as message content
	 * @param conversationId   identifier of the conversation
	 * @param protocol         message protocol
	 * @return request ACLMessage
	 */
	public static ACLMessage preparePowerCheckRequest(final GreenEnergyAgent greenEnergyAgent, final Object requestData,
			final String conversationId, final String protocol) {
		final ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		try {
			request.setContent(JsonMapper.getMapper().writeValueAsString(requestData));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		request.addReceiver(greenEnergyAgent.getMonitoringAgent());
		request.setConversationId(conversationId);
		request.setProtocol(protocol);
		return request;
	}
}
