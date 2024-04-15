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
import org.greencloud.gui.messages.domain.GreenSourceCreator;
import org.greencloud.gui.messages.domain.ServerCreator;
import org.greencloud.gui.websocket.EGCSWebSocketClient;
import org.jrba.rulesengine.ruleset.domain.ModifyAgentRuleSetEvent;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.connector.factory.EGCSControllerFactory;
import com.greencloud.connector.factory.AgentFactory;
import com.greencloud.connector.factory.AgentFactoryImpl;
import com.greencloud.connector.factory.AgentNodeFactory;
import com.greencloud.connector.factory.AgentNodeFactoryImpl;

import jade.wrapper.AgentController;

public class EventListener extends EGCSWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

	private final Map<String, EGCSNode> agentNodes;
	private final AgentFactory agentFactory;
	private final AgentNodeFactory nodeFactory;
	private EGCSControllerFactory factory;

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
	public void connectWithAgentFactory(final EGCSControllerFactory factory) {
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

	/**
	 * Method creates new server based on the passed server data
	 *
	 * @param serverCreator data of new server
	 */
	public void createNewServer(final ServerCreator serverCreator) {
		final ServerArgs serverArgs = agentFactory.createServerAgent(serverCreator);
		final ServerNode serverNode = nodeFactory.createServerNode(serverArgs);

		final AgentController serverAgentController = factory.createAgentController(serverArgs, serverNode);
		factory.runAgentController(serverAgentController, RUN_AGENT_DELAY);
	}

	/**
	 * Method creates new server based on the passed green source data
	 *
	 * @param greenSourceCreator data of new green source
	 */
	public void createNewGreenSource(final GreenSourceCreator greenSourceCreator) {
		final String monitoringName = "Monitoring" + greenSourceCreator.getName();
		final GreenEnergyArgs greenEnergyArgs = agentFactory.createGreenEnergyAgent(greenSourceCreator, monitoringName);

		final MonitoringArgs monitoringArgs = agentFactory.createMonitoringAgent(monitoringName);
		final MonitoringNode monitoringNode = nodeFactory.createMonitoringNode(monitoringArgs,
				greenEnergyArgs.getName());
		final AgentController monitoringAgentController = factory.createAgentController(monitoringArgs,
				monitoringNode);
		factory.runAgentController(monitoringAgentController, RUN_AGENT_DELAY);

		final AgentController greenSourceAgentController = factory.createAgentController(greenEnergyArgs);
		factory.runAgentController(greenSourceAgentController, RUN_AGENT_DELAY);
	}

	/**
	 * Method triggers weather drop event
	 *
	 * @param weatherDropEvent data related to weather drop
	 */
	public void triggerWeatherDrop(final WeatherDropEvent weatherDropEvent) {
		weatherDropEvent.trigger(agentNodes);
	}

	/**
	 * Method disables selected server
	 *
	 * @param disableServerEvent data specifying information of server that is to be disabled
	 */
	public void switchServerOff(final DisableServerEvent disableServerEvent) {
		disableServerEvent.trigger(agentNodes);
	}

	/**
	 * Method enables selected server
	 *
	 * @param enableServerEvent data specifying information of server that is to be enabled
	 */
	public void switchServerOn(final EnableServerEvent enableServerEvent) {
		enableServerEvent.trigger(agentNodes);
	}

	/**
	 * Method modifies current rule set of selected system region
	 *
	 * @param modifyAgentRuleSetEvent data specifying information about next rule set modification
	 */
	public void modifyRuleSet(final ModifyAgentRuleSetEvent modifyAgentRuleSetEvent) {
		modifyAgentRuleSetEvent.trigger(agentNodes);
	}

	/**
	 * Method modifies resources of the selected server
	 *
	 * @param serverMaintenanceEvent data specifying information new server resources
	 */
	public void performServerMaintenance(final ServerMaintenanceEvent serverMaintenanceEvent) {
		serverMaintenanceEvent.trigger(agentNodes);
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
			createNewGreenSource(greenSourceCreationEvent.getGreenSourceCreator());
		}
		if (message.contains("SERVER_CREATION_EVENT")) {
			final ServerCreationEvent serverCreationEvent = ServerCreationEvent.create(message);
			createNewServer(serverCreationEvent.getServerCreator());
		}
	}
}
