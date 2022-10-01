package com.gui.agents;

import static com.greencloud.commons.job.JobStatusEnum.CREATED;

import java.util.concurrent.atomic.AtomicReference;

import com.greencloud.commons.args.client.ClientAgentArgs;
import com.greencloud.commons.job.JobStatusEnum;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetClientJobStatusMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the client
 */
public class ClientAgentNode extends AbstractAgentNode {

	private final ClientAgentArgs args;
	private final AtomicReference<JobStatusEnum> jobStatusEnum;

	/**
	 * Client node constructor
	 *
	 * @param args arguments provided for client agent creation
	 */
	public ClientAgentNode(ClientAgentArgs args) {
		super(args.getName());
		this.args = args;
		this.jobStatusEnum = new AtomicReference<>(CREATED);
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("CLIENT")
				.data(args)
				.build());
	}

	/**
	 * Function overrides the job status
	 *
	 * @param jobStatusEnum new job status
	 */
	public void updateJobStatus(final JobStatusEnum jobStatusEnum) {
		this.jobStatusEnum.set(jobStatusEnum);
		webSocketClient.send(ImmutableSetClientJobStatusMessage.builder()
				.data(jobStatusEnum.getStatus())
				.agentName(agentName)
				.build());
	}
}
