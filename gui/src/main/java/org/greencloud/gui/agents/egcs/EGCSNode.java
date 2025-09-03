package org.greencloud.gui.agents.egcs;

import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getClientsWebSocket;

import java.time.Instant;
import java.util.Objects;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.gui.messages.ImmutableRegisterAgentMessage;
import org.greencloud.gui.messages.ImmutableRemoveAgentMessage;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.agentmodel.domain.node.AgentNode;
import org.jrba.agentmodel.domain.props.AgentProps;
import org.jrba.environment.websocket.GuiWebSocketClient;

import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.database.knowledge.types.DataType;

import lombok.Getter;
import lombok.Setter;

/**
 * Class represents abstract generic agent node
 */
@Getter
@Setter
public abstract class EGCSNode<T extends AgentArgs, E extends AgentProps> extends AgentNode<E>
		implements EGCSNodeInterface {

	protected TimescaleDatabase databaseClient;
	protected T nodeArgs;

	protected EGCSNode() {
		super();
	}

	/**
	 * Class constructor
	 *
	 * @param nodeArgs  arguments used to create agent node
	 * @param agentType type of agent node
	 */
	protected EGCSNode(T nodeArgs, EGCSAgentType agentType) {
		super(nodeArgs.getName(), agentType.name());
		this.nodeArgs = nodeArgs;
	}

	@Override
	public void addToGraph() {
		getClientsWebSocket().send(ImmutableRegisterAgentMessage.builder()
				.agentType(agentType)
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
	public void writeMonitoringData(final DataType dataType, final MonitoringData monitoringData, final String name) {
		databaseClient.writeMonitoringData(name, dataType, monitoringData);
	}

	/**
	 * Method writes monitoring data to database
	 *
	 * @param time           time of monitoring data reporting
	 * @param dataType       type of the data that is to be written
	 * @param monitoringData data that is to be written
	 * @param name           name of agent for which data is written
	 */
	public void writeMonitoringData(final Instant time, final DataType dataType, final MonitoringData monitoringData,
			final String name) {
		databaseClient.writeMonitoringData(time, name, dataType, monitoringData);
	}

	/**
	 * Method writes initial configuration of an agent into the database.
	 *
	 * @param agentName    name of the agent
	 * @param agentAddress address of the agent
	 * @param agentType    agent type
	 * @param agentConfig  configuration of an agent
	 */
	public void writeAgentConfiguration(final String agentName, final String agentAddress,
			final String agentType, final AgentArgs agentConfig) {
		databaseClient.writeAgentData(agentName, agentAddress, agentType, agentConfig);
	}

	@Override
	public String getAgentName() {
		return agentName;
	}

	@Override
	public GuiWebSocketClient initializeSocket(String url) {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		EGCSNode<T, E> agentNode = (EGCSNode<T, E>) o;
		return agentName.equals(agentNode.agentName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentName);
	}
}
