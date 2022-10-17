package com.gui.agents;

import static java.lang.Double.parseDouble;

import java.util.Optional;

import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.greenenergy.ImmutableGreenEnergyNodeArgs;
import com.greencloud.commons.location.ImmutableLocation;
import com.greencloud.commons.location.Location;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the green energy source
 */
public class GreenEnergyAgentNode extends AbstractNetworkAgentNode {

	private final Location location;
	private final String monitoringAgent;
	private final String serverAgent;
	private final String energyType;

	/**
	 * Green energy source node constructor
	 *
	 * @param args arguments provided for green energy agent creation
	 */
	public GreenEnergyAgentNode(GreenEnergyAgentArgs args) {
		super(args.getName(), parseDouble(args.getMaximumCapacity()));
		this.location = ImmutableLocation.builder()
				.latitude(Double.parseDouble(args.getLatitude()))
				.longitude(Double.parseDouble(args.getLongitude()))
				.build();
		this.serverAgent = args.getOwnerSever();
		this.monitoringAgent = args.getMonitoringAgent();
		this.energyType = args.getEnergyType();
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("GREEN_ENERGY")
				.data(ImmutableGreenEnergyNodeArgs.builder()
						.maximumCapacity(String.valueOf(initialMaximumCapacity))
						.name(agentName)
						.agentLocation(location)
						.monitoringAgent(monitoringAgent)
						.serverAgent(serverAgent)
						.energyType(energyType)
						.build())
				.build());
	}

	public Optional<PowerShortageEvent> getEvent() {
		return Optional.ofNullable((PowerShortageEvent) eventsQueue.poll());
	}
}
