package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;

import org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants;
import org.greencloud.commons.utils.messaging.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used to communicate that the system topology has been altered (i.e. new agents added/removed)
 */
public class AgentDiscoveryMessageFactory {
	/**
	 * Method creates the message sent to managing agent after new network component is successfully created
	 * (in result of system adaptation)
	 *
	 * @param containerName name of the container in which new agent reside
	 * @param agentName     name of a new agent
	 * @param managingAgent AID of managing agent to which the message is sent
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareMessageToManagingAgent(final String containerName, final String agentName,
			final AID managingAgent) {
		final String protocol = String.join("_", MessageProtocolConstants.CONFIRM_SYSTEM_PLAN_MESSAGE, agentName, containerName);
		return MessageBuilder.builder(0)
				.withPerformative(INFORM)
				.withMessageProtocol(protocol)
				.withStringContent(protocol)
				.withReceivers(managingAgent)
				.build();
	}
}
