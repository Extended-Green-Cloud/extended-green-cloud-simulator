package com.gui.agents;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.greencloud.commons.args.agent.client.ClientAgentArgs;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetClientJobDurationMapMessage;
import com.gui.message.ImmutableSetClientJobStatusMessage;
import com.gui.message.ImmutableSetClientJobTimeFrameMessage;
import com.gui.message.ImmutableSplitJobMessage;
import com.gui.message.domain.ImmutableJobStatus;
import com.gui.message.domain.ImmutableJobTimeFrame;
import com.gui.message.domain.ImmutableSplitJob;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the client
 */
public class ClientAgentNode extends AbstractAgentNode {

	private ClientAgentArgs args;

	public ClientAgentNode() {
		super();
	}

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
	 * @param clientJobStatusEnum new job status
	 */
	public void updateJobStatus(final JobClientStatusEnum clientJobStatusEnum) {
		webSocketClient.send(ImmutableSetClientJobStatusMessage.builder()
				.data(ImmutableJobStatus.builder()
						.status(clientJobStatusEnum.getStatus())
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
						.start(jobPart.getStartTime())
						.end(jobPart.getEndTime())
						.splitJobId(jobPart.getJobId())
						.build()).toList())
				.jobId(args.getJobId())
				.build());
	}

	/**
	 * Function informs about the job status for a part of job
	 *
	 * @param clientJobStatusEnum new job status
	 */
	public void updateJobStatus(final JobClientStatusEnum clientJobStatusEnum, String jobPartId) {
		webSocketClient.send(ImmutableSetClientJobStatusMessage.builder()
				.data(ImmutableJobStatus.builder()
						.status(clientJobStatusEnum.getStatus())
						.splitJobId(jobPartId)
						.build())
				.agentName(agentName)
				.build());
	}

	/**
	 * Function informs about the job time frame change for a job
	 *
	 * @param jobStart new job start time
	 * @param jobEnd   new job end time
	 */
	public void updateJobTimeFrame(final Instant jobStart, final Instant jobEnd) {
		webSocketClient.send(ImmutableSetClientJobTimeFrameMessage.builder()
				.data(ImmutableJobTimeFrame.builder()
						.start(jobStart)
						.end(jobEnd)
						.build())
				.agentName(agentName)
				.build());
	}

	/**
	 * Function informs about the job time frame change for a part of job
	 *
	 * @param jobStart new job start time
	 * @param jobEnd   new job end time
	 */
	public void updateJobTimeFrame(final Instant jobStart, final Instant jobEnd, String jobPartId) {
		webSocketClient.send(ImmutableSetClientJobTimeFrameMessage.builder()
				.data(ImmutableJobTimeFrame.builder()
						.start(jobStart)
						.end(jobEnd)
						.splitJobId(jobPartId)
						.build())
				.agentName(agentName)
				.build());
	}

	/**
	 * Function informs about the duration of job execution at given statuses
	 *
	 * @param durationMap map of job duration
	 */
	public void updateJobDurationMap(final Map<JobClientStatusEnum, Long> durationMap) {
		webSocketClient.send(ImmutableSetClientJobDurationMapMessage.builder()
				.data(durationMap)
				.agentName(agentName)
				.build());
	}

}
