package com.greencloud.application.agents.client.management;

import static com.database.knowledge.domain.agent.DataType.CLIENT_JOB_EXECUTION;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.CREATED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.PROCESSED;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.SCHEDULED;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientJobExecutionData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.greencloud.application.agents.AbstractAgentManagement;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;
import com.gui.agents.ClientAgentNode;

/**
 * Class stores methods used to manage the state of Client Agent
 */
public class ClientManagement extends AbstractAgentManagement {

	private static final List<JobClientStatusEnum> SIMULATION_STATUSES = List.of(SCHEDULED, PROCESSED, CREATED);
	protected ClientAgent clientAgent;

	/**
	 * Default constructor that initializes the job status duration map and the timer
	 *
	 * @param clientAgent client using the state management
	 */
	public ClientManagement(final ClientAgent clientAgent) {
		this.clientAgent = clientAgent;
	}

	/**
	 * Method updates the job status of original job in case of job split
	 *
	 * @param status new status
	 */
	public void updateOriginalJobStatus(final JobClientStatusEnum status) {
		if (isOriginalStatusUpdated(status)) {
			if (nonNull(clientAgent.getAgentNode())) {
				((ClientAgentNode) clientAgent.getAgentNode()).updateJobStatus(status);
			}
			clientAgent.getJobExecution().setJobStatus(status);
		}
	}

	/**
	 * Method writes current state of Client's job to the database
	 *
	 * @param isFinished flag indicating if the state is final
	 */
	public void writeClientData(final boolean isFinished) {
		final Map<JobClientStatusEnum, Long> jobDurationMap = getJobStatusDurationMap().entrySet().stream()
				.collect(filtering(entry -> List.of(ON_BACK_UP, IN_PROGRESS).contains(entry.getKey()),
						toMap(Map.Entry::getKey, Map.Entry::getValue)));

		final ClientMonitoringData data = ImmutableClientMonitoringData.builder()
				.isFinished(isFinished)
				.currentJobStatus(clientAgent.getJobExecution().getJobStatus())
				.jobStatusDurationMap(jobDurationMap)
				.build();

		clientAgent.writeMonitoringData(CLIENT_MONITORING, data);
		updateJobDurationMapGUI();

		if (isFinished) {
			writeJobExecutionPercentage();
		}
	}

	private void writeJobExecutionPercentage() {
		final long executionTime = getJobStatusDurationMap().get(IN_PROGRESS);
		final long expectedExecution = Duration.between(clientAgent.getJobExecution().getJobSimulatedStart(),
				clientAgent.getJobExecution().getJobSimulatedEnd()).toMillis();
		final double executedPercentage = expectedExecution == 0 ? 0 : (double) executionTime / expectedExecution;

		clientAgent.writeMonitoringData(CLIENT_JOB_EXECUTION, ImmutableClientJobExecutionData.builder()
				.jobExecutionPercentage(executedPercentage)
				.build());
		((ClientAgentNode) clientAgent.getAgentNode()).updateJobExecutionPercentage(executedPercentage);
	}

	private void updateJobDurationMapGUI() {
		final ToLongFunction<Map.Entry<JobClientStatusEnum, Long>> getRealDuration = entry ->
				SIMULATION_STATUSES.contains(entry.getKey()) ? entry.getValue() : convertToRealTime(entry.getValue());

		final Map<JobClientStatusEnum, Long> guiJobDurationMap = getJobStatusDurationMap().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, getRealDuration::applyAsLong));

		((ClientAgentNode) clientAgent.getAgentNode()).updateJobDurationMap(guiJobDurationMap);
	}

	private Map<JobClientStatusEnum, Long> getJobStatusDurationMap() {
		return clientAgent.getJobExecution().getJobDurationMap();
	}

	private boolean isOriginalStatusUpdated(final JobClientStatusEnum status) {
		return switch (status) {
			case SCHEDULED, FINISHED -> clientAgent.getJobExecution().getJobStatus().equals(status);
			case PROCESSED -> clientAgent.getJobExecution().getJobStatus().equals(SCHEDULED);
			case DELAYED, IN_PROGRESS -> clientAgent.getJobExecution().getJobStatus().equals(PROCESSED);
			case ON_BACK_UP -> List.of(IN_PROGRESS, PROCESSED).contains(clientAgent.getJobExecution().getJobStatus());
			case ON_HOLD -> true;
			default -> false;
		};
	}
}
