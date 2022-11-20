package com.greencloud.application.agents.client.management;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.greencloud.commons.job.JobStatusEnum.CREATED;
import static com.greencloud.commons.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.JobStatusEnum.SCHEDULED;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.database.knowledge.domain.agent.ClientMonitoringData;
import com.database.knowledge.domain.agent.ImmutableClientMonitoringData;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.utils.domain.Timer;
import com.greencloud.commons.job.JobStatusEnum;
import com.gui.agents.ClientAgentNode;

/**
 * Class store methods used to manage the state of Client Agent
 */
public class ClientStateManagement {

	protected final Timer timer = new Timer();
	protected JobStatusEnum currentJobStatus;
	protected Map<JobStatusEnum, Long> jobStatusDurationMap;
	protected ClientAgent clientAgent;

	/**
	 * Default constructor that initializes the job status duration map and the timer
	 *
	 * @param clientAgent client using the state management
	 */
	public ClientStateManagement(final ClientAgent clientAgent) {
		this.clientAgent = clientAgent;
		currentJobStatus = CREATED;
		jobStatusDurationMap = Arrays.stream(JobStatusEnum.values())
				.collect(Collectors.toMap(status -> status, status -> 0L));
		timer.startTimeMeasure();
	}

	/**
	 * Method updates the job duration map
	 *
	 * @param newStatus new job status
	 */
	public synchronized void updateJobStatusDuration(final JobStatusEnum newStatus) {
		final long elapsedTime = timer.stopTimeMeasure();
		timer.startTimeMeasure();
		jobStatusDurationMap.computeIfPresent(currentJobStatus, (key, val) -> val + elapsedTime);
		currentJobStatus = newStatus;
	}

	/**
	 * Method updates the job status of original job in case of job split
	 *
	 * @param status new status
	 */
	public void updateOriginalJobStatus(final JobStatusEnum status) {
		if (isOriginalStatusUpdated(status)) {
			if (Objects.nonNull(clientAgent.getAgentNode())) {
				((ClientAgentNode) clientAgent.getAgentNode()).updateJobStatus(status);
			}
			currentJobStatus = status;
		}
	}

	/**
	 * Method verifies if all job parts have a given status
	 *
	 * @param status status to verify
	 * @return boolean
	 */
	public boolean checkIfAllPartsMatchStatus(final JobStatusEnum status) {
		return clientAgent.getJobParts().values().stream().map(JobPart::getStatus).allMatch(status::equals);
	}

	/**
	 * Method writes current state of Client's job to the database
	 *
	 * @param isFinished flag indicating if the state is final
	 */
	public void writeClientData(final boolean isFinished) {
		final ClientMonitoringData data = ImmutableClientMonitoringData.builder()
				.currentJobStatus(currentJobStatus)
				.jobStatusDurationMap(getJobStatusDurationMap())
				.isFinished(isFinished)
				.build();
		clientAgent.writeMonitoringData(CLIENT_MONITORING, data);
	}

	public JobStatusEnum getCurrentJobStatus() {
		return currentJobStatus;
	}

	public void setCurrentJobStatus(JobStatusEnum currentJobStatus) {
		this.currentJobStatus = currentJobStatus;
	}

	public Timer getTimer() {
		return timer;
	}

	private Map<JobStatusEnum, Long> getJobStatusDurationMap() {
		if (clientAgent.isSplit()) {
			final Map<JobStatusEnum, Long> result = new EnumMap<>(JobStatusEnum.class);
			clientAgent.getJobParts().values().stream()
					.map(JobPart::getJobStatusDurationMap)
					.flatMap(map -> map.entrySet().stream()
							.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue())))
					.forEach(entry -> result.merge(entry.getKey(), entry.getValue(), Long::sum));
			return result;
		}
		return jobStatusDurationMap;
	}

	private boolean isOriginalStatusUpdated(final JobStatusEnum status) {
		return switch (status) {
			case SCHEDULED, FINISHED -> checkIfAllPartsMatchStatus(status);
			case PROCESSED -> currentJobStatus.equals(SCHEDULED);
			case DELAYED -> currentJobStatus.equals(PROCESSED);
			case ON_BACK_UP -> List.of(IN_PROGRESS, PROCESSED).contains(currentJobStatus);
			case IN_PROGRESS -> checkIfAllPartsMatchStatus(IN_PROGRESS) || currentJobStatus.equals(PROCESSED);
			case ON_HOLD -> true;
			default -> false;
		};
	}
}
