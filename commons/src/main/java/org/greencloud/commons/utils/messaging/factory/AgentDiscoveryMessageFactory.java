package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CHANGE_SERVER_RESOURCES_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CONFIRM_SYSTEM_PLAN_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REGISTER_SERVER_RESOURCES_PROTOCOL;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
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
	 * @param serverAgentProps properties of the given Server
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareResourceInformationMessage(final ServerAgentProps serverAgentProps,
			final AID cna, final int ruleSetIdx) {
		return MessageBuilder.builder(ruleSetIdx)
				.withPerformative(INFORM)
				.withMessageProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withObjectContent(ImmutableServerResources.builder()
						.resources(serverAgentProps.resources())
						.price(serverAgentProps.getPricePerHour())
						.build())
				.withReceivers(cna)
				.build();
	}

	/**
	 * Message send to Server asking about its resources
	 *
	 * @param server server asked about resources
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareRequestForResourceInformationMessage(final AID server, final int ruleSetIdx) {
		return MessageBuilder.builder(ruleSetIdx)
				.withPerformative(REQUEST)
				.withMessageProtocol(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withObjectContent(REGISTER_SERVER_RESOURCES_PROTOCOL)
				.withReceivers(server)
				.build();
	}

	/**
	 * Message send to CNA informing that resources of the given server have changed
	 *
	 * @param serverAgentProps properties of the given Server
	 * @return inform ACLMessage
	 */
	public static ACLMessage prepareRequestInformingCNAAboutResourceChange(final ServerAgentProps serverAgentProps,
			final Map<String, Resource> newResources, final int ruleSetIdx) {
		return MessageBuilder.builder(ruleSetIdx)
				.withPerformative(REQUEST)
				.withMessageProtocol(CHANGE_SERVER_RESOURCES_PROTOCOL)
				.withObjectContent(ImmutableServerResources.builder()
						.resources(newResources)
						.price(serverAgentProps.getPricePerHour())
						.build())
				.withReceivers(serverAgentProps.getOwnerCloudNetworkAgent())
				.build();
	}
}
