package com.gui.controller;

import java.net.URI;
import java.util.Objects;

import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableUpdateSingleValueMessage;
import com.gui.websocket.GuiWebSocketClient;
import com.gui.websocket.GuiWebSocketListener;

public class GuiControllerImpl implements GuiController {

	private static GuiWebSocketClient webSocketClient;
	private static GuiWebSocketListener webSocketListener;

	public GuiControllerImpl() {
	}

	public GuiControllerImpl(String mainHostUri) {
		if (webSocketClient == null) {
			webSocketClient = new GuiWebSocketClient(URI.create(mainHostUri));
		}
		if (webSocketListener == null) {
			webSocketListener = new GuiWebSocketListener(URI.create(mainHostUri + "powerShortage"));
		}
	}

	@Override
	public void run() {
		webSocketClient.connect();
		webSocketListener.connect();
	}

	@Override
	public void addAgentNodeToGraph(AbstractAgentNode agent) {
		if (Objects.nonNull(agent)) {
			webSocketListener.addAgentNode(agent);
			agent.addToGraph(webSocketClient);
		}
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
	public void updateFailedJobsCountByValue(int value) {
		webSocketClient.send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("INCREMENT_FAILED_JOBS")
				.build());
	}

	@Override
	public void updateFinishedJobsCountByValue(int value) {
		webSocketClient.send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("INCREMENT_FINISHED_JOBS")
				.build());
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName) {
		webSocketListener.triggerPowerShortage(powerShortageEvent, agentName);
	}
}
