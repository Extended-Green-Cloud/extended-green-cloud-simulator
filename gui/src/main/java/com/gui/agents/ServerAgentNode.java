package com.gui.agents;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.greencloud.commons.args.server.ImmutableServerNodeArgs;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the server
 */
public class ServerAgentNode extends AbstractNetworkAgentNode {

	private final String cloudNetworkAgent;
	private final List<String> greenEnergyAgents;
	private final AtomicReference<Double> backUpTraffic;
	private final AtomicInteger totalNumberOfClients;

	/**
	 * Server node constructor
	 *
	 * @param name              node name
	 * @param maximumCapacity   maximum server capacity
	 * @param cloudNetworkAgent name of the owner cloud network
	 * @param greenEnergyAgents names of owned green sources
	 */
	public ServerAgentNode(
			String name,
			double maximumCapacity,
			String cloudNetworkAgent,
			List<String> greenEnergyAgents) {
		super(name, maximumCapacity);
		this.cloudNetworkAgent = cloudNetworkAgent;
		this.greenEnergyAgents = greenEnergyAgents;
		this.totalNumberOfClients = new AtomicInteger(0);
		this.backUpTraffic = new AtomicReference<>(0D);
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("SERVER")
				.data(ImmutableServerNodeArgs.builder()
						.name(agentName)
						.maximumCapacity(initialMaximumCapacity)
						.cloudNetworkAgent(cloudNetworkAgent)
						.greenEnergyAgents(greenEnergyAgents)
						.build())
				.build());
	}

	/**
	 * Function updates the current back-up traffic to given value
	 *
	 * @param backUpPowerInUse current power in use coming from back-up energy
	 */
	public void updateBackUpTraffic(final double backUpPowerInUse) {
		this.backUpTraffic.set(initialMaximumCapacity != 0 ? ((backUpPowerInUse / initialMaximumCapacity) * 100) : 0);
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(backUpPowerInUse)
				.agentName(agentName)
				.type("SET_SERVER_BACK_UP_TRAFFIC")
				.build());
	}

	/**
	 * Function updates the number of clients
	 *
	 * @param value new clients count
	 */
	public void updateClientNumber(final int value) {
		this.totalNumberOfClients.set(value);
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}
}
