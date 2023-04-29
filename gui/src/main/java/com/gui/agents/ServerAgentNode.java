package com.gui.agents;

import java.util.List;
import java.util.Optional;

import com.greencloud.commons.args.agent.server.ImmutableServerNodeArgs;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableDisableServerMessage;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.websocket.GuiWebSocketClient;

import jade.util.leap.Serializable;

/**
 * Agent node class representing the server
 */
public class ServerAgentNode extends AbstractNetworkAgentNode implements Serializable {

	private String cloudNetworkAgent;
	private List<String> greenEnergyAgents;

	public ServerAgentNode() {
		super();
	}

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
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("SERVER")
				.data(ImmutableServerNodeArgs.builder()
						.name(agentName)
						.maximumCapacity(initialMaximumCapacity.get())
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
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}

	/**
	 * Function disables the server
	 */
	public void disableServer() {
		webSocketClient.send(ImmutableDisableServerMessage.builder()
				.cna(cloudNetworkAgent)
				.server(agentName)
				.capacity(initialMaximumCapacity.get())
				.build());
	}

	public Optional<PowerShortageEvent> getEvent() {
		return Optional.ofNullable((PowerShortageEvent) eventsQueue.poll());
	}
}
