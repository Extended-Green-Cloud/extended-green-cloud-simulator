package com.greencloud.connector.gui;

import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_AGENT_DELAY;
import static com.greencloud.connector.factory.constants.AgentControllerConstants.RUN_CLIENT_AGENT_DELAY;
import static java.util.Optional.ofNullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
import org.greencloud.gui.messages.domain.EventData;
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.greencloud.gui.websocket.EGCSWebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jrba.environment.domain.ExternalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;
import com.greencloud.connector.factory.AgentNodeFactory;
import com.greencloud.connector.factory.AgentNodeFactoryImpl;
import com.greencloud.connector.factory.EGCSControllerFactory;

import jade.wrapper.AgentController;
import lombok.Setter;

@SuppressWarnings("rawtypes")
public class EventListenerImpl extends EGCSWebSocketClient implements EventListener {

	private static final Logger logger = LoggerFactory.getLogger(EventListenerImpl.class);

	private final Map<String, Consumer<String>> handlerMap;
	private final Map<String, EGCSNode> agentNodes;
	private final AgentFactory agentFactory;
	private final AgentNodeFactory nodeFactory;
	@Setter
	private EGCSControllerFactory factory;

	public EventListenerImpl(final URI serverUri) {
		super(serverUri);
		agentNodes = new HashMap<>();
		agentFactory = new AgentFactoryImpl();
		nodeFactory = new AgentNodeFactoryImpl();
		handlerMap = initializeEventHandlers();
	}

	@Override
	public void addAgentNode(final EGCSNode agentNode) {
		ofNullable(agentNode).ifPresent(node -> agentNodes.put(agentNode.getAgentName(), node));
	}

	@Override
	public void triggerEvent(final ExternalEvent event) {
		event.trigger(agentNodes);
	}

	@Override
	public void triggerEvent(final EventData eventData) {
		switch (eventData) {
			case ServerCreator serverCreator -> createNewServer(serverCreator);
			case GreenSourceCreator greenSourceCreator -> createNewGreenSource(greenSourceCreator);
			default -> logger.info("Handler not specified!");
		}
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		logger.info("Connected to message listener");
	}

	@Override
	public void onMessage(final String message) {
		logger.info("Received message: {}", message);
		handlerMap.entrySet().stream()
				.filter(entry -> message.contains(entry.getKey()))
				.findFirst()
				.ifPresent(entry -> entry.getValue().accept(message));

	}

	private Map<String, Consumer<String>> initializeEventHandlers() {
		return Map.of(
				"POWER_SHORTAGE_EVENT", msg -> PowerShortageEvent.create(msg).trigger(agentNodes),
				"WEATHER_DROP_EVENT", msg -> WeatherDropEvent.create(msg).trigger(agentNodes),
				"SWITCH_OFF_EVENT", msg -> DisableServerEvent.create(msg).trigger(agentNodes),
				"SWITCH_ON_EVENT", msg -> EnableServerEvent.create(msg).trigger(agentNodes),
				"SERVER_MAINTENANCE_EVENT", msg -> ServerMaintenanceEvent.create(msg).trigger(agentNodes),
				"CLIENT_CREATION_EVENT", this::handleClientCreation,
				"GREEN_SOURCE_CREATION_EVENT", this::handleNewGreenSourceCreation,
				"SERVER_CREATION_EVENT", this::handleNewServerCreation
		);
	}

	private void handleClientCreation(final String message) {
		final ClientCreationEvent clientCreationEvent = ClientCreationEvent.create(message);
		final int jobId = factory.getDatabase().getNextClientId();

		final ClientArgs clientArgs = agentFactory.createClientAgent(clientCreationEvent.getJobCreator(),
				clientCreationEvent.getClientName(), jobId);
		final AgentController agentController = factory.createAgentController(clientArgs);
		factory.runAgentController(agentController, RUN_CLIENT_AGENT_DELAY);
	}

	private void handleNewGreenSourceCreation(final String message) {
		final GreenSourceCreator greenSourceCreator = GreenSourceCreationEvent.create(message).getGreenSourceCreator();
		createNewGreenSource(greenSourceCreator);
	}

	private void handleNewServerCreation(final String message) {
		final ServerCreator serverCreator = ServerCreationEvent.create(message).getServerCreator();
		createNewServer(serverCreator);
	}

	private void createNewServer(final ServerCreator serverCreator) {
		final ServerArgs serverArgs = agentFactory.createServerAgent(serverCreator);
		final ServerNode serverNode = nodeFactory.createServerNode(serverArgs);

		final AgentController serverAgentController = factory.createAgentController(serverArgs, serverNode);
		factory.runAgentController(serverAgentController, RUN_AGENT_DELAY);
	}

	private void createNewGreenSource(final GreenSourceCreator greenSourceCreator) {
		final String monitoringName = "Monitoring" + greenSourceCreator.getName();
		final GreenEnergyArgs greenEnergyArgs = agentFactory.createGreenEnergyAgent(greenSourceCreator, monitoringName);
		final MonitoringArgs monitoringArgs = agentFactory.createMonitoringAgent(monitoringName);
		final MonitoringNode monitoringNode =
				nodeFactory.createMonitoringNode(monitoringArgs, greenEnergyArgs.getName());

		final AgentController monitoringAgent = factory.createAgentController(monitoringArgs, monitoringNode);
		factory.runAgentController(monitoringAgent, RUN_AGENT_DELAY);

		final AgentController greenSourceAgent = factory.createAgentController(greenEnergyArgs);
		factory.runAgentController(greenSourceAgent, RUN_AGENT_DELAY);
	}
}
