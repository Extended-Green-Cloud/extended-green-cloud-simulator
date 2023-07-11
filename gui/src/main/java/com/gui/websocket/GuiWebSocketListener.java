package com.gui.websocket;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.PowerShortageMessage;

public class GuiWebSocketListener extends GuiWebSocketClient {

	private static final Logger logger = LoggerFactory.getLogger(GuiWebSocketListener.class);

	private final Map<String, AbstractAgentNode> agentNodes;
	private final TimescaleDatabase database;

	public GuiWebSocketListener(URI serverUri, final TimescaleDatabase database) {
		super(serverUri);
		agentNodes = new HashMap<>();
		this.database = database;
	}

	public void addAgentNode(AbstractAgentNode agentNode) {
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
		AbstractAgentNode agentNode = agentNodes.get(agentName);

		if (Objects.isNull(agentNode)) {
			logger.error("Agent {} was not found. Power shortage couldn't be triggered", agentName);
			return;
		}
		agentNode.addEvent(powerShortageEvent);
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
	}

	private void handlePowerShortageMessage(String message) {
		PowerShortageMessage powerShortageMessage = readPowerShortage(message);
		PowerShortageEvent powerShortageEvent = new PowerShortageEvent(powerShortageMessage);
		triggerPowerShortage(powerShortageEvent, powerShortageMessage.getAgentName());
	}

	private PowerShortageMessage readPowerShortage(String message) {
		try {
			return MAPPER.readValue(message, PowerShortageMessage.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
