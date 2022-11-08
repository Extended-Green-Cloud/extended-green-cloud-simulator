package com.gui.controller;

import java.net.URI;
import java.util.List;

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

	@Override
	public void displayMessageArrow(AbstractAgentNode senderAgent, List<String> receiversNames) {
		/*TODO should be uncommented or removed
		webSocketClient.send(ImmutableArrowMessage.builder()
				.type("DISPLAY_MESSAGE_ARROW")
				.agentName(senderAgent.getAgentName())
				.data(receiversNames)
				.build());
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.schedule(() -> webSocketClient.send(ImmutableArrowMessage.builder()
						.type("HIDE_MESSAGE_ARROW")
						.agentName(senderAgent.getAgentName())
						.data(receiversNames)
						.build()),
				500, TimeUnit.MILLISECONDS);*/
	}
}
