package com.gui.agents;

import com.gui.websocket.GuiWebSocketClient;

public interface AbstractAgentNodeInterface {

	/**
	 * Method retrieves the agent name
	 *
	 * @return agent name
	 */
	String getAgentName();

	/**
	 * Method responsible for adding the node to the graph
	 */
	void addToGraph(GuiWebSocketClient webSocketClient);
}
