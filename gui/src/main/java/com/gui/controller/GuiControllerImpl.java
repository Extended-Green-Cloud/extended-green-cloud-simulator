package com.gui.controller;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static com.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static com.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static com.gui.websocket.WebSocketConnections.getEventSocket;
import static com.gui.websocket.WebSocketConnections.getManagingSystemSocket;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.gui.agents.EGCSNode;
import com.gui.event.PowerShortageEvent;
import com.gui.websocket.WebSocketConnections;
import com.gui.websocket.enums.SocketTypeEnum;

public class GuiControllerImpl implements GuiController {

	public GuiControllerImpl(final Map<SocketTypeEnum, String> hostUris) {
		WebSocketConnections.initialize(hostUris);
	}

	@Override
	public void run() {
		WebSocketConnections.connect();
	}

	@Override
	public void reportSystemStartTime(final Instant time) {
		getAgentsWebSocket().reportSystemStartTime(time);
		getClientsWebSocket().reportSystemStartTime(time);
		getManagingSystemSocket().reportSystemStartTime(time);
		getCloudNetworkSocket().reportSystemStartTime(time);
		getEventSocket().reportSystemStartTime(time);
	}

	@Override
	public void addAgentNodeToGraph(final EGCSNode agent) {
		if (Objects.nonNull(agent)) {
			getEventSocket().addAgentNode(agent);
			agent.addToGraph();
		}
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName) {
		getEventSocket().triggerPowerShortage(powerShortageEvent, agentName);
	}
}
