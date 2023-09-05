package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;

import java.util.Optional;

import com.greencloud.commons.args.agent.server.ServerNodeArgs;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableDisableServerMessage;
import com.gui.message.ImmutableEnableServerMessage;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateResourcesMessage;

import jade.util.leap.Serializable;

/**
 * Agent node class representing the server
 */
public class ServerAgentNode extends AbstractNetworkAgentNode implements Serializable {

	private ServerNodeArgs serverNodeArgs;

	public ServerAgentNode() {
		super();
	}

	/**
	 * Server node constructor
	 *
	 * @param serverNodeArgs aarguments of given server node
	 */
	public ServerAgentNode(ServerNodeArgs serverNodeArgs) {
		super(serverNodeArgs.getName());
		this.serverNodeArgs = serverNodeArgs;
	}

	@Override
	public void addToGraph() {
		getAgentsWebSocket().send(ImmutableRegisterAgentMessage.builder()
				.agentType("SERVER")
				.data(serverNodeArgs)
				.build());
	}

	/**
	 * Function updates the current back-up traffic to given value
	 *
	 * @param backUpPowerInUse current power in use coming from back-up energy
	 */
	public void updateBackUpTraffic(final double backUpPowerInUse) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(backUpPowerInUse)
				.agentName(agentName)
				.type("SET_SERVER_BACK_UP_TRAFFIC")
				.build());
	}

	/**
	 * Function updates current in-use resources
	 *
	 * @param resources              currently utilized resources
	 * @param powerConsumption       current power consumption
	 * @param powerConsumptionBackUp current back-up power consumption
	 */
	public void updateResources(final HardwareResources resources, final double powerConsumption,
			final double powerConsumptionBackUp) {
		getAgentsWebSocket().send(ImmutableUpdateResourcesMessage.builder()
				.resources(resources)
				.powerConsumption(powerConsumption)
				.powerConsumptionBackUp(powerConsumptionBackUp)
				.agentName(agentName)
				.build());
	}

	/**
	 * Function updates the number of clients
	 *
	 * @param value new clients count
	 */
	public void updateClientNumber(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}

	/**
	 * Function disables the server
	 */
	public void disableServer() {
		getAgentsWebSocket().send(ImmutableDisableServerMessage.builder()
				.cna(serverNodeArgs.getCloudNetworkAgent())
				.server(agentName)
				.cpu(serverNodeArgs.getCpu())
				.build());
	}

	/**
	 * Function enables the server
	 */
	public void enableServer() {
		getAgentsWebSocket().send(ImmutableEnableServerMessage.builder()
				.cna(serverNodeArgs.getCloudNetworkAgent())
				.server(agentName)
				.cpu(serverNodeArgs.getCpu())
				.build());
	}

	public Optional<PowerShortageEvent> getEvent() {
		return Optional.ofNullable((PowerShortageEvent) eventsQueue.poll());
	}
}
