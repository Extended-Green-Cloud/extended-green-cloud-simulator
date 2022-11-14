package com.gui.agents;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.event.domain.AbstractEvent;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Class represents abstract generic agent node
 */
public abstract class AbstractAgentNode implements AbstractAgentNodeInterface {

	protected String agentName;
	protected GuiWebSocketClient webSocketClient;
	protected TimescaleDatabase databaseClient;
	protected Queue<AbstractEvent> eventsQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Class constructor
	 *
	 * @param agentName name of the agent
	 */
	protected AbstractAgentNode(String agentName) {
		this.agentName = agentName;
	}

	@Override
	public String getAgentName() {
		return agentName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractAgentNode agentNode = (AbstractAgentNode) o;
		return agentName.equals(agentNode.agentName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentName);
	}

	public void addEvent(AbstractEvent event) {
		eventsQueue.add(event);
	}

	public void setDatabaseClient(TimescaleDatabase databaseClient) {
		this.databaseClient = databaseClient;
	}

	public TimescaleDatabase getDatabaseClient() {
		return databaseClient;
	}
}
