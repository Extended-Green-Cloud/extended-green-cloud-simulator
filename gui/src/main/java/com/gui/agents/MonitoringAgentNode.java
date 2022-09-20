package com.gui.agents;

import com.greencloud.commons.args.monitoring.ImmutableMonitoringNodeArgs;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the monitoring agent
 */
public class MonitoringAgentNode extends AbstractAgentNode {

	private final String greenEnergyAgent;

	/**
	 * Monitoring node constructor
	 *
	 * @param name             node name
	 * @param greenEnergyAgent owner green energy agent
	 */
	public MonitoringAgentNode(String name, String greenEnergyAgent) {
		super(name);
		this.greenEnergyAgent = greenEnergyAgent;
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("MONITORING")
				.data(ImmutableMonitoringNodeArgs.builder()
						.name(agentName)
						.greenEnergyAgent(greenEnergyAgent)
						.build())
				.build());
	}
}
