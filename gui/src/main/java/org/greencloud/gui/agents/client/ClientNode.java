package org.greencloud.gui.agents.client;

import static java.time.Duration.between;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.gui.websocket.WebSocketConnections.getClientsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.agent.client.node.ClientNodeArgs;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.greencloud.commons.utils.time.TimeConverter;
import org.greencloud.commons.utils.time.TimeSimulation;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.ImmutableSetClientJobDurationMapMessage;
import org.greencloud.gui.messages.ImmutableSetClientJobStatusMessage;
import org.greencloud.gui.messages.ImmutableSetClientJobTimeFrameMessage;
import org.greencloud.gui.messages.ImmutableUpdateJobExecutionProportionMessage;
import org.greencloud.gui.messages.ImmutableUpdateSingleValueMessage;
import org.greencloud.gui.messages.domain.ImmutableJobTimeFrame;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientJobExecutionData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientStatisticsData;

/**
 * Agent node class representing the client
 */
public class ClientNode extends EGCSNode<ClientNodeArgs, ClientAgentProps> {

	private static final List<JobClientStatusEnum> SIMULATION_STATUSES = List.of(JobClientStatusEnum.SCHEDULED,
			JobClientStatusEnum.PROCESSED, JobClientStatusEnum.CREATED);

	private boolean isFinished;

	public ClientNode() {
		super();
		this.isFinished = false;
	}

	/**
	 * Client node constructor
	 *
	 * @param args arguments provided for client agent creation
	 */
	public ClientNode(ClientNodeArgs args) {
		super(args, AgentType.CLIENT);
	}

	/**
	 * Function announce new client in the system
	 */
	public void announceNewClient() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("UPDATE_CURRENT_CLIENTS")
				.build());
	}

	/**
	 * Function removes client from the system
	 */
	public void removeClient() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(-1)
				.type("UPDATE_CURRENT_CLIENTS")
				.build());
	}

	/**
	 * Function removes client job from the system
	 */
	public void removeClientJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("INCREMENT_FINISHED_JOBS")
				.build());
	}

	/**
	 * Function increments number of jobs finished in cloud
	 */
	public void incrementFinishedInCloud() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("INCREMENT_FINISHED_IN_CLOUD_JOBS")
				.build());
	}

	/**
	 * Function informs that the client job has failed
	 */
	public void announceFailedJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("INCREMENT_FAILED_JOBS")
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
	 * @param agentProps properties of Client Agent
	 */
	public void updateJobDurationMap(final ClientAgentProps agentProps) {
		final ToLongFunction<Map.Entry<JobClientStatusEnum, Long>> getRealDuration = entry ->
				SIMULATION_STATUSES.contains(entry.getKey()) ?
						entry.getValue() :
						TimeConverter.convertToRealTime(entry.getValue());

		final Map<JobClientStatusEnum, Long> durationMap = agentProps.getJobDurationMap().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, getRealDuration::applyAsLong));

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

	/**
	 * Method passes the information about message retrieval time to the database
	 *
	 * @param jobUpdate data of job update
	 */
	public void measureTimeToRetrieveTheMessage(final JobWithStatus jobUpdate, final ClientAgentProps agentProps) {
		final Instant timeWhenTheMessageWasSent = jobUpdate.getChangeTime();
		final Instant timeWhenTheMessageWasReceived = TimeSimulation.getCurrentTime();
		final long elapsedTime = Duration.between(timeWhenTheMessageWasSent, timeWhenTheMessageWasReceived).toMillis();

		writeMonitoringData(DataType.CLIENT_STATISTICS, ImmutableClientStatisticsData.builder()
				.messageRetrievalTime(elapsedTime)
				.build(), agentProps.getAgentName());
	}

	@Override
	public void updateGUI(final ClientAgentProps props) {
		// no GUI node to apply
	}

	@Override
	public void saveMonitoringData(final ClientAgentProps props) {
		final Map<JobClientStatusEnum, Long> jobDurationMap = props.getJobDurationMap().entrySet().stream()
				.collect(filtering(entry -> List.of(JobClientStatusEnum.ON_BACK_UP, JobClientStatusEnum.IN_PROGRESS)
								.contains(entry.getKey()),
						toMap(Map.Entry::getKey, Map.Entry::getValue)));

		final ClientMonitoringData data = ImmutableClientMonitoringData.builder()
				.isFinished(isFinished)
				.currentJobStatus(props.getJobStatus())
				.jobStatusDurationMap(jobDurationMap)
				.build();

		writeMonitoringData(DataType.CLIENT_MONITORING, data, props.getAgentName());
		updateJobDurationMap(props);

		if (isFinished) {
			writeJobExecutionPercentage(props);
		}
	}

	private void writeJobExecutionPercentage(final ClientAgentProps props) {
		final long executionTime =
				props.getJobDurationMap().get(JobClientStatusEnum.IN_PROGRESS) + props.getJobDurationMap().get(
						JobClientStatusEnum.ON_BACK_UP);
		final long expectedExecution = between(props.getJobSimulatedStart(), props.getJobSimulatedEnd()).toMillis();
		final double executedPercentage = expectedExecution == 0 ? 0 : (double) executionTime / expectedExecution;

		writeMonitoringData(DataType.CLIENT_JOB_EXECUTION, ImmutableClientJobExecutionData.builder()
				.jobExecutionPercentage(executedPercentage)
				.build(), props.getAgentName());
		updateJobExecutionPercentage(executedPercentage);
	}

	public void setFinished(final boolean finished) {
		isFinished = finished;
	}

	private double clampJobPercentage(final double value) {
		return Math.max(Math.min(1, value), 0);
	}
}
