package com.greencloud.connector.gui;

import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_CLIENT_AGENT_DELAY;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.agents.monitoring.MonitoringNode;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.gui.event.ClientCreationEvent;
import org.greencloud.gui.event.DisableServerEvent;
import org.greencloud.gui.event.EnableServerEvent;
import org.greencloud.gui.event.GreenSourceCreationEvent;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.ServerCreationEvent;
import org.greencloud.gui.event.ServerMaintenanceEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.greencloud.gui.websocket.GuiWebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.connector.factory.AgentControllerFactory;
import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;
import com.greencloud.connector.factory.AgentNodeFactory;
import com.greencloud.connector.factory.AgentNodeFactoryImpl;

import jade.wrapper.AgentController;

public class EventListener extends GuiWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

	private final Map<String, EGCSNode> agentNodes;
	private final AgentFactory agentFactory;
	private final AgentNodeFactory nodeFactory;
	private AgentControllerFactory factory;

	public EventListener(final URI serverUri) {
		super(serverUri);
		agentNodes = new HashMap<>();
		agentFactory = new AgentFactoryImpl();
		nodeFactory = new AgentNodeFactoryImpl();
	}

	/**
	 * Method used to connect the event listener with agent factory
	 *
	 * @param factory agent factory that is to be connected with the event listener
	 */
	public void connectWithAgentFactory(final AgentControllerFactory factory) {
		this.factory = factory;
	}

	/**
	 * Method used to connect new agent node with a listener
	 *
	 * @param agentNode node that is to be taken into account in event listener
	 */
	public void addAgentNode(EGCSNode agentNode) {
		if (Objects.nonNull(agentNode)) {
			agentNodes.put(agentNode.getAgentName(), agentNode);
		}
	}

	/**
	 * Method triggers power shortage event in the specified agent
	 *
	 * @param powerShortageEvent data for the power shortage event
	 */
	public void triggerPowerShortage(PowerShortageEvent powerShortageEvent) {
		powerShortageEvent.trigger(agentNodes);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		logger.info("Connected to message listener");
	}

	@Override
	public void onMessage(String message) {
		logger.info("Received message: {}", message);
		if (message.contains("POWER_SHORTAGE_EVENT")) {
			PowerShortageEvent.create(message).trigger(agentNodes);
		}
		if (message.contains("WEATHER_DROP_EVENT")) {
			WeatherDropEvent.create(message).trigger(agentNodes);
		}
		if (message.contains("SWITCH_OFF_EVENT")) {
			DisableServerEvent.create(message).trigger(agentNodes);
		}
		if (message.contains("SWITCH_ON_EVENT")) {
			EnableServerEvent.create(message).trigger(agentNodes);
		}
		if (message.contains("SERVER_MAINTENANCE_EVENT")) {
			ServerMaintenanceEvent.create(message).trigger(agentNodes);
		}
		if (message.contains("CLIENT_CREATION_EVENT")) {
			final ClientCreationEvent clientCreationEvent = ClientCreationEvent.create(message);
			final int jobId = factory.getDatabase().getNextClientId();

			final ClientArgs clientArgs = agentFactory.createClientAgent(clientCreationEvent.getJobCreator(),
					clientCreationEvent.getClientName(), jobId);
			final AgentController agentController = factory.createAgentController(clientArgs);
			factory.runAgentController(agentController, RUN_CLIENT_AGENT_DELAY);
		}
		if (message.contains("GREEN_SOURCE_CREATION_EVENT")) {
			final GreenSourceCreationEvent greenSourceCreationEvent = GreenSourceCreationEvent.create(message);
			final String monitoringName = "Monitoring" + greenSourceCreationEvent.getGreenSourceCreator().getName();

			final GreenEnergyArgs greenEnergyArgs = agentFactory.createGreenEnergyAgent(
					greenSourceCreationEvent.getGreenSourceCreator(), monitoringName);

			final MonitoringArgs monitoringArgs = agentFactory.createMonitoringAgent(monitoringName);
			final MonitoringNode monitoringNode = nodeFactory.createMonitoringNode(monitoringArgs,
					greenEnergyArgs.getName());
			final AgentController monitoringAgentController = factory.createAgentController(monitoringArgs,
					monitoringNode);
			factory.runAgentController(monitoringAgentController, RUN_AGENT_DELAY);

			final AgentController greenSourceAgentController = factory.createAgentController(greenEnergyArgs);
			factory.runAgentController(greenSourceAgentController, RUN_AGENT_DELAY);
		}
		if (message.contains("SERVER_CREATION_EVENT")) {
			final ServerCreationEvent serverCreationEvent = ServerCreationEvent.create(message);

			final ServerArgs serverArgs = agentFactory.createServerAgent(serverCreationEvent.getServerCreator());
			final ServerNode serverNode = nodeFactory.createServerNode(serverArgs);

			final AgentController serverAgentController = factory.createAgentController(serverArgs, serverNode);
			factory.runAgentController(serverAgentController, RUN_AGENT_DELAY);
		}
	}
}
