package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONFIRM_SYSTEM_PLAN_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REGISTER_SERVER_RESOURCES_PROTOCOL;

import java.util.Map;

import org.greencloud.commons.domain.agent.ImmutableServerResources;
import org.greencloud.commons.domain.resources.Resource;
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
		final String protocol = String.join("_", CONFIRM_SYSTEM_PLAN_MESSAGE, agentName, containerName);
		return MessageBuilder.builder(0)
				.withPerformative(INFORM)
				.withMessageProtocol(protocol)
				.withStringContent(protocol)
				.withReceivers(managingAgent)
				.build();
	}

	/**
	 * Message send to CNA informing about resources of new Server
	 *
	 * @param serverResources resources of the Server
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareResourceInformationMessage(final Map<String, Resource> serverResources,
			final AID cna, final int strategyIdx) {
		return MessageBuilder.builder(strategyIdx)
				.withPerformative(INFORM)
				.withMessageProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withObjectContent(ImmutableServerResources.builder().resources(serverResources).build())
				.withReceivers(cna)
				.build();
	}

	/**
	 * Message send to Server asking about its resources
	 *
	 * @param server server asked about resources
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareRequestForResourceInformationMessage(final AID server, final int strategyIdx) {
		return MessageBuilder.builder(strategyIdx)
				.withPerformative(REQUEST)
				.withMessageProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withObjectContent(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withReceivers(server)
				.build();
	}
}
