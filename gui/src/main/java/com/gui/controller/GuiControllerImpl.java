package com.gui.controller;

import java.net.URI;
import java.util.List;
import com.gui.agents.AbstractAgentNode;
import com.gui.message.ImmutableUpdateSingleValueMessage;
import com.gui.websocket.GuiWebSocketClient;

public class GuiControllerImpl implements GuiController {

	private final GuiWebSocketClient webSocketClient;

	public GuiControllerImpl(String mainHostUri) {
		webSocketClient = new GuiWebSocketClient(URI.create(mainHostUri));
	}

	@Override
	public void run() {
		webSocketClient.connect();
	}

	@Override
	public void addAgentNodeToGraph(AbstractAgentNode agent) {
		agent.addToGraph(webSocketClient);
	}

	@Override
	public void removeAgentNodeFromGraph(AbstractAgentNode agentNode) {
		// TODO
	}

	@Override
	public void createEdges() {
		// TODO FOR REMOVAL?
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
	public void displayMessageArrow(AbstractAgentNode senderAgent, List<String> receiversNames) {
//		webSocketClient.send(ImmutableArrowMessage.builder()
//				.type("DISPLAY_MESSAGE_ARROW")
//				.agentName(senderAgent.getAgentName())
//				.data(receiversNames)
//				.build());
//		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//		executorService.schedule(() -> webSocketClient.send(ImmutableArrowMessage.builder()
//						.type("HIDE_MESSAGE_ARROW")
//						.agentName(senderAgent.getAgentName())
//						.data(receiversNames)
//						.build()),
//				500, TimeUnit.MILLISECONDS);
	}
}
