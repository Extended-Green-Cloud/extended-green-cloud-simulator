package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;

import java.util.List;

import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkNodeArgs;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;

/**
 * Agent node class representing the cloud network
 */
public class CloudNetworkAgentNode extends AbstractNetworkAgentNode {

	private final List<String> serverAgents;
	private final double maxServersCpu;

	/**
	 * Cloud network node constructor
	 *
	 * @param name         name of the node
	 * @param maxCpu       maximal servers cpu
	 * @param serverAgents list of server agents names
	 */
	public CloudNetworkAgentNode(String name, final double maxCpu, List<String> serverAgents) {
		super(name);
		this.serverAgents = serverAgents;
		this.maxServersCpu = maxCpu;
	}

	@Override
	public void addToGraph() {
		getAgentsWebSocket().send(ImmutableRegisterAgentMessage.builder()
				.agentType("CLOUD_NETWORK")
				.data(ImmutableCloudNetworkNodeArgs.builder()
						.name(agentName)
						.maxServersCpu(maxServersCpu)
						.serverAgents(serverAgents)
						.build())
				.build());
	}

	/**
	 * Function updates the number of clients to given value
	 *
	 * @param value value indicating the client number
	 */
	public void updateClientNumber(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}
}
