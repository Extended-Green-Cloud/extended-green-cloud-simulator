package com.gui.controller;

import java.net.URI;

import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableUpdateSingleValueMessage;
import com.gui.websocket.GuiWebSocketClient;
import com.gui.websocket.GuiWebSocketListener;

public class GuiControllerImpl implements GuiController {

	private final GuiWebSocketClient webSocketClient;
	private final GuiWebSocketListener webSocketListener;

	public GuiControllerImpl(String mainHostUri) {
		webSocketClient = new GuiWebSocketClient(URI.create(mainHostUri));
		webSocketListener = new GuiWebSocketListener(URI.create(mainHostUri + "powerShortage"));
	}

	@Override
	public void run() {
		webSocketClient.connect();
		webSocketListener.connect();
	}

	@Override
	public void addAgentNodeToGraph(AbstractAgentNode agent) {
		webSocketListener.addAgentNode(agent);
		agent.addToGraph(webSocketClient);
	}

	@Override
	public void removeAgentNodeFromGraph(AbstractAgentNode agentNode) {
		// TODO
	}

	@Override
	public void updateClientsCountByValue(int value) {
		webSocketClient.send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_CLIENTS")
				.build());
	}

	@Override
	public void updateActiveJobsCountByValue(int value) {
		webSocketClient.send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_ACTIVE_JOBS")
				.build());
	}

	@Override
	public void updateAllJobsCountByValue(int value) {
		webSocketClient.send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_PLANNED_JOBS")
				.build());
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName) {
		webSocketListener.triggerPowerShortage(powerShortageEvent, agentName);
	}
}
