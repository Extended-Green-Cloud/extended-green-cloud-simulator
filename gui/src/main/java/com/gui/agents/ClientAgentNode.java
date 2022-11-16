package com.gui.agents;

import java.util.List;

import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.JobStatusEnum;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetClientJobStatusMessage;
import com.gui.message.ImmutableSplitJobMessage;
import com.gui.message.domain.ImmutableJobStatus;
import com.gui.message.domain.ImmutableSplitJob;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the client
 */
public class ClientAgentNode extends AbstractAgentNode {

	private final ClientAgentArgs args;

	/**
	 * Client node constructor
	 *
	 * @param args arguments provided for client agent creation
	 */
	public ClientAgentNode(ClientAgentArgs args) {
		super(args.getName());
		this.args = args;
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
		webSocketClient.send(ImmutableSetClientJobStatusMessage.builder()
				.data(ImmutableJobStatus.builder()
						.status(jobStatusEnum.getStatus())
						.splitJobId(null)
						.build())
				.agentName(agentName)
				.build());
	}

	/**
	 * Function to inform about a job split
	 *
	 * @param jobParts job parts created after the split
	 */
	public void informAboutSplitJob(List<ClientJob> jobParts) {
		webSocketClient.send(ImmutableSplitJobMessage.builder()
				.addAllData(jobParts.stream().map(jobPart -> ImmutableSplitJob.builder()
						.power(jobPart.getPower())
						.startDate(jobPart.getStartTime())
						.endDate(jobPart.getEndTime())
						.splitJobId(jobPart.getJobId())
						.build()).toList())
				.jobId(args.getJobId())
				.build());
	}

	/**
	 * Function informs about the job status for a part of job
	 *
	 * @param jobStatusEnum new job status
	 */
	public void updateJobStatus(final JobStatusEnum jobStatusEnum, String jobPartId) {
		webSocketClient.send(ImmutableSetClientJobStatusMessage.builder()
				.data(ImmutableJobStatus.builder()
						.status(jobStatusEnum.getStatus())
						.splitJobId(jobPartId)
						.build())
				.agentName(agentName)
				.build());
	}

}
