package com.greencloud.connector.gui;

import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getManagingSystemSocket;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.websocket.WebSocketConnections;
import org.greencloud.gui.websocket.enums.SocketTypeEnum;

import com.greencloud.connector.factory.AgentControllerFactory;

public class GuiControllerImpl implements GuiController {

	private final EventListener eventSocket;

	public GuiControllerImpl(final Map<SocketTypeEnum, String> hostUris) {
		WebSocketConnections.initialize(hostUris);
		eventSocket = new EventListener(
				URI.create(hostUris.get(SocketTypeEnum.EVENTS_WEB_SOCKET) + "event"));
	}

	@Override
	public void run() {
		WebSocketConnections.connect();
		eventSocket.connect();
	}

	@Override
	public void connectWithAgentFactory(final AgentControllerFactory factory) {
		eventSocket.connectWithAgentFactory(factory);
	}

	@Override
	public void reportSystemStartTime(final Instant time) {
		getAgentsWebSocket().reportSystemStartTime(time);
		getClientsWebSocket().reportSystemStartTime(time);
		getManagingSystemSocket().reportSystemStartTime(time);
		getCloudNetworkSocket().reportSystemStartTime(time);
		eventSocket.reportSystemStartTime(time);
	}

	@Override
	public void addAgentNodeToGraph(final EGCSNode agent) {
		if (Objects.nonNull(agent)) {
			eventSocket.addAgentNode(agent);
			agent.addToGraph();
		}
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent) {
		eventSocket.triggerPowerShortage(powerShortageEvent);
	}
}
