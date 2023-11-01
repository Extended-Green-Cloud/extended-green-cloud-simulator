package org.greencloud.gui.agents;

import org.greencloud.commons.args.agent.AgentNodeProps;
import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.gui.websocket.GuiWebSocketClient;

import lombok.Getter;

/**
 * Class represents generic agent node
 */
@Getter
public abstract class AgentNode<E extends AgentProps> implements AgentNodeProps<E> {

	protected GuiWebSocketClient mainWebSocket;
	protected String agentName;
	protected String agentType;

	public AgentNode() {
	}

	/**
	 * Class constructor
	 *
	 * @param name      agent name
	 * @param agentType type of agent node
	 */
	protected AgentNode(final String name, String agentType) {
		this.agentName = name;
		this.agentType = agentType;
	}

	/**
	 * Method that can be used to initialize node communication socket
	 */
	public GuiWebSocketClient initializeSocket(final String url) {
		return null;
	}

	/**
	 * Method that can be used to initialize node communication socket
	 */
	public void connectSocket(final String url) {
		mainWebSocket = initializeSocket(url);
		mainWebSocket.connect();
	}
}
