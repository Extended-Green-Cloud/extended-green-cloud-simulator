package com.gui.websocket;

import static org.greencloud.commons.args.agent.AgentType.SCHEDULER;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.agents.egcs.EGCSNode;
import com.gui.agents.cloudnetwork.CloudNetworkNode;
import com.gui.agents.scheduler.SchedulerNode;
import com.gui.agents.server.ServerNode;
import com.gui.event.PowerShortageEvent;
import com.gui.event.WeatherDropEvent;
import com.gui.message.PowerShortageMessage;
import com.gui.message.WeatherDropMessage;

public class GuiWebSocketListener extends GuiWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(GuiWebSocketListener.class);

	private final Map<String, EGCSNode> agentNodes;

	public GuiWebSocketListener(URI serverUri) {
		super(serverUri);
		agentNodes = new HashMap<>();
	}

	public void addAgentNode(EGCSNode agentNode) {
		if (Objects.nonNull(agentNode)) {
			agentNodes.put(agentNode.getAgentName(), agentNode);
		}
	}

	/**
	 * Method triggers power shortage event in the specified agent
	 *
	 * @param powerShortageEvent data for the power shortage event
	 * @param agentName          agent for which the event is triggered
	 */
	public void triggerPowerShortage(PowerShortageEvent powerShortageEvent, String agentName) {
		EGCSNode agentNode = agentNodes.get(agentName);

		if (Objects.isNull(agentNode)) {
			logger.error("Agent {} was not found. Power shortage couldn't be triggered", agentName);
			return;
		}
		agentNode.addEvent(powerShortageEvent);
	}

	/**
	 * Method triggers weather drop event in the specified agent
	 *
	 * @param weatherDropEvent data for the power shortage event
	 * @param agentName        agent for which the event is triggered
	 */
	public void triggerWeatherDrop(WeatherDropEvent weatherDropEvent, String agentName) {
		final CloudNetworkNode agentNode = (CloudNetworkNode) agentNodes.get(agentName);
		final SchedulerNode schedulerNode = (SchedulerNode) agentNodes.values().stream()
				.filter(node -> node.getAgentType().equals(SCHEDULER.name()))
				.findFirst().orElseThrow();

		if (Objects.isNull(agentNode)) {
			logger.error("Agent {} was not found. Weather drop couldn't be triggered", agentName);
			return;
		}

		final List<EGCSNode> greenEnergyNodes = agentNode.getNodeArgs().getServerAgents().stream()
				.map(agentNodes::get)
				.map(ServerNode.class::cast)
				.map(server -> server.getNodeArgs().getGreenEnergyAgents())
				.flatMap(Collection::stream)
				.map(agentNodes::get)
				.toList();

		greenEnergyNodes.forEach(node -> node.addEvent(weatherDropEvent));
		schedulerNode.addEvent(weatherDropEvent);
		agentNode.addEvent(weatherDropEvent);
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		logger.info("Connected to message listener");
	}

	@Override
	public void onMessage(String message) {
		logger.info("Received message: {}", message);
		if (message.contains("POWER_SHORTAGE_EVENT")) {
			handlePowerShortageMessage(message);
		}
		if (message.contains("WEATHER_DROP_EVENT")) {
			handleWeatherDropEventMessage(message);
		}
	}

	private void handlePowerShortageMessage(String message) {
		PowerShortageMessage powerShortageMessage = readPowerShortage(message);
		PowerShortageEvent powerShortageEvent = new PowerShortageEvent(powerShortageMessage);
		triggerPowerShortage(powerShortageEvent, powerShortageMessage.getAgentName());
	}

	private void handleWeatherDropEventMessage(String message) {
		WeatherDropMessage weatherDropMessage = readWeatherDropMessage(message);
		WeatherDropEvent weatherDropEvent = new WeatherDropEvent(weatherDropMessage);
		triggerWeatherDrop(weatherDropEvent, weatherDropMessage.getAgentName());
	}

	private PowerShortageMessage readPowerShortage(String message) {
		try {
			return mapper.readValue(message, PowerShortageMessage.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private WeatherDropMessage readWeatherDropMessage(String message) {
		try {
			return mapper.readValue(message, WeatherDropMessage.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
