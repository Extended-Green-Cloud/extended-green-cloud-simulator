package com.gui.websocket;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gui.agents.egcs.EGCSNode;
import com.gui.event.DisableServerEvent;
import com.gui.event.EnableServerEvent;
import com.gui.event.PowerShortageEvent;
import com.gui.event.ServerMaintenanceEvent;
import com.gui.event.WeatherDropEvent;

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
	}
}
