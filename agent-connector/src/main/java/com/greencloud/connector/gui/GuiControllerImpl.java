package com.greencloud.connector.gui;

import static java.net.URI.create;
import static org.greencloud.gui.websocket.WebSocketConnections.connect;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getManagingSystemSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.initialize;
import static org.greencloud.gui.websocket.enums.SocketTypeEnum.EVENTS_WEB_SOCKET;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.domain.EventData;
import org.greencloud.gui.websocket.enums.SocketTypeEnum;
import org.jrba.environment.domain.ExternalEvent;

import com.greencloud.connector.factory.EGCSControllerFactory;

@SuppressWarnings("rawtypes")
public class GuiControllerImpl implements GuiController {

	private final EventListenerImpl eventSocket;

	public GuiControllerImpl(final Map<SocketTypeEnum, String> hostUris) {
		initialize(hostUris);
		eventSocket = new EventListenerImpl(create(hostUris.get(EVENTS_WEB_SOCKET) + "event"));
	}

	@Override
	public void run() {
		connect();
		eventSocket.connect();
	}

	@Override
	public void connectWithAgentFactory(final EGCSControllerFactory factory) {
		eventSocket.setFactory(factory);
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
	public void triggerEvent(final ExternalEvent event) {
		eventSocket.triggerEvent(event);
	}

	@Override
	public void triggerEvent(final EventData eventData) {
		eventSocket.triggerEvent(eventData);
	}
}
