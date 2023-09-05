package com.gui.agents;

import static com.gui.websocket.WebSocketConnections.getClientsWebSocket;

import java.time.Instant;
import java.util.Map;

import com.greencloud.commons.args.agent.client.ClientNodeArgs;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetClientJobDurationMapMessage;
import com.gui.message.ImmutableSetClientJobStatusMessage;
import com.gui.message.ImmutableSetClientJobTimeFrameMessage;
import com.gui.message.ImmutableUpdateJobExecutionProportionMessage;
import com.gui.message.domain.ImmutableJobTimeFrame;

/**
 * Agent node class representing the client
 */
public class ClientAgentNode extends AbstractAgentNode {

	private ClientNodeArgs args;

	public ClientAgentNode() {
		super();
	}

	/**
	 * Client node constructor
	 *
	 * @param args arguments provided for client agent creation
	 */
	public ClientAgentNode(ClientNodeArgs args) {
		super(args.getName());
		this.args = args;
	}

	@Override
	public void addToGraph() {
		getClientsWebSocket().send(ImmutableRegisterAgentMessage.builder()
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
		getClientsWebSocket().send(ImmutableSetClientJobStatusMessage.builder()
				.status(clientJobStatusEnum.getStatus())
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
		getClientsWebSocket().send(ImmutableSetClientJobTimeFrameMessage.builder()
				.data(ImmutableJobTimeFrame.builder()
						.start(jobStart)
						.end(jobEnd)
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
		getClientsWebSocket().send(ImmutableSetClientJobDurationMapMessage.builder()
				.data(durationMap)
				.agentName(agentName)
				.build());
	}

	/**
	 * Function informs about the final job execution percentage
	 * (i.e. how much of the job has been successfully executed)
	 *
	 * @param executionPercentage job execution percentage
	 */
	public void updateJobExecutionPercentage(final Double executionPercentage) {
		getClientsWebSocket().send(ImmutableUpdateJobExecutionProportionMessage.builder()
				.data(clampJobPercentage(executionPercentage))
				.agentName(agentName)
				.build());
	}

	private double clampJobPercentage(final double value) {
		return Math.max(Math.min(1, value), 0);
	}
}
