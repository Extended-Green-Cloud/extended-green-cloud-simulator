package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static com.gui.websocket.WebSocketConnections.getClientsWebSocket;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.AgentNodeProps;
import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.args.agent.AgentType;
import com.gui.event.AbstractEvent;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableRemoveAgentMessage;

import lombok.Getter;

/**
 * Class represents abstract generic agent node
 */
@Getter
public abstract class AbstractNode<T extends AgentArgs, E extends AgentProps>
		implements AbstractNodeInterface, AgentNodeProps<E> {

	protected final Queue<AbstractEvent> eventsQueue = new ConcurrentLinkedQueue<>();
	protected TimescaleDatabase databaseClient;
	protected String agentName;
	protected T nodeArgs;
	protected AgentType agentType;


	protected AbstractNode() {
	}

	/**
	 * Class constructor
	 *
	 * @param nodeArgs  arguments used to create agent node
	 * @param agentType type of agent node
	 */
	protected AbstractNode(T nodeArgs, AgentType agentType) {
		this.agentName = nodeArgs.getName();
		this.nodeArgs = nodeArgs;
		this.agentType = agentType;
	}

	@Override
	public void addToGraph() {
		getClientsWebSocket().send(ImmutableRegisterAgentMessage.builder()
				.agentType(agentType.name())
				.data(nodeArgs)
				.build());
	}

	@Override
	public void removeAgentNodeFromGraph() {
		getAgentsWebSocket().send(ImmutableRemoveAgentMessage.builder()
				.agentName(agentName)
				.build());
	}

	/**
	 * Method writes monitoring data to database
	 *
	 * @param dataType       type of the data that is to be written
	 * @param monitoringData data that is to be written
	 * @param name           name of agent for which data is written
	 */
	public void writeMonitoringData(DataType dataType, MonitoringData monitoringData, String name) {
		databaseClient.writeMonitoringData(name, dataType, monitoringData);
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
		AbstractNode<T, E> agentNode = (AbstractNode<T, E>) o;
		return agentName.equals(agentNode.agentName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentName);
	}

	public void addEvent(AbstractEvent event) {
		eventsQueue.add(event);
	}

	public TimescaleDatabase getDatabaseClient() {
		return databaseClient;
	}

	public void setDatabaseClient(TimescaleDatabase databaseClient) {
		this.databaseClient = databaseClient;
	}
}
