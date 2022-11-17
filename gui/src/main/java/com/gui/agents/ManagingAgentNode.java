package com.gui.agents;

import com.greencloud.commons.args.agent.managing.ManagingAgentArgs;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the managing agent
 */
public class ManagingAgentNode extends AbstractAgentNode {

	final double qualityThreshold;

	/**
	 * Managing agent node constructor
	 *
	 * @param args arguments provided for managing agent creation
	 */
	public ManagingAgentNode(ManagingAgentArgs args) {
		super(args.getName());

		this.qualityThreshold = args.getSystemQualityThreshold();
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		//TO BE IMPLEMENTED
	}
}
