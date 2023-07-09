package com.gui.controller;

import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static com.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static com.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static com.gui.websocket.WebSocketConnections.getEventSocket;
import static com.gui.websocket.WebSocketConnections.getManagingSystemSocket;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.AbstractAgentNode;
import com.gui.event.domain.PowerShortageEvent;
import com.gui.message.ImmutableUpdateSingleValueMessage;
import com.gui.websocket.WebSocketConnections;
import com.gui.websocket.enums.SocketTypeEnum;

public class GuiControllerImpl implements GuiController {

	public GuiControllerImpl(final Map<SocketTypeEnum, String> hostUris, final TimescaleDatabase databaseClient) {
		WebSocketConnections.initialize(hostUris, databaseClient);
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
	public void addAgentNodeToGraph(final AbstractAgentNode agent) {
		if (Objects.nonNull(agent)) {
			getEventSocket().addAgentNode(agent);
			agent.addToGraph();
		}
	}

	@Override
	public void updateFailedJobsCountByValue(final int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("INCREMENT_FAILED_JOBS")
				.build());
	}

	@Override
	public void updateFinishedJobsCountByValue(final int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("INCREMENT_FINISHED_JOBS")
				.build());
	}

	@Override
	public void updateJobsFinishedInCloudCountByValue(int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("INCREMENT_FINISHED_JOBS_CLOUD")
				.build());
	}

	@Override
	public void updateClientsCountByValue(final int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_CLIENTS")
				.build());
	}

	@Override
	public void updateActiveJobsCountByValue(final int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_ACTIVE_JOBS")
				.build());
	}

	@Override
	public void updateActiveInCloudJobsCountByValue(int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_ACTIVE_JOBS_CLOUD")
				.build());
	}

	@Override
	public void updateAllJobsCountByValue(final int value) {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(value)
				.type("UPDATE_CURRENT_PLANNED_JOBS")
				.build());
	}

	@Override
	public void triggerPowerShortageEvent(final PowerShortageEvent powerShortageEvent, final String agentName) {
		getEventSocket().triggerPowerShortage(powerShortageEvent, agentName);
	}
}
