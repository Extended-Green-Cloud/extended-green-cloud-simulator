package com.greencloud.application.agents.client.management;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.job.ClientJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.ClientJobStatusEnum.SCHEDULED;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToLongFunction;

import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.utils.domain.Timer;
import com.greencloud.commons.job.ClientJobStatusEnum;
import com.gui.agents.ClientAgentNode;

/**
 * Class store methods used to manage the state of Client Agent
 */
public class ClientStateManagement {

	protected final Timer timer = new Timer();
	protected ClientJobStatusEnum currentJobStatus;
	protected Map<ClientJobStatusEnum, Long> jobStatusDurationMap;
	protected ClientAgent clientAgent;

	/**
	 * Default constructor that initializes the job status duration map and the timer
	 *
	 * @param clientAgent client using the state management
	 */
	public ClientStateManagement(final ClientAgent clientAgent) {
		this.clientAgent = clientAgent;
		currentJobStatus = CREATED;
		jobStatusDurationMap = Arrays.stream(ClientJobStatusEnum.values())
				.collect(toMap(status -> status, status -> 0L));
		timer.startTimeMeasure(getCurrentTime());
	}

	/**
	 * Method updates the job duration map
	 *
	 * @param newStatus new job status
	 */
	public synchronized void updateJobStatusDuration(final ClientJobStatusEnum newStatus, final Instant time) {
		final long elapsedTime = timer.stopTimeMeasure(time);
		timer.startTimeMeasure(time);
		jobStatusDurationMap.computeIfPresent(currentJobStatus, (key, val) -> val + elapsedTime);
		currentJobStatus = newStatus;
	}

	/**
	 * Method updates the job status of original job in case of job split
	 *
	 * @param status new status
	 */
	public void updateOriginalJobStatus(final ClientJobStatusEnum status) {
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
	public boolean checkIfAllPartsMatchStatus(final ClientJobStatusEnum status) {
		return clientAgent.getJobParts().values().stream().map(JobPart::getStatus).allMatch(status::equals);
	}

	/**
	 * Method writes current state of Client's job to the database
	 *
	 * @param isFinished flag indicating if the state is final
	 */
	public void writeClientData(final boolean isFinished) {
		var jobDurationMap = getJobStatusDurationMap().entrySet().stream()
				.collect(filtering(entry -> List.of(ON_BACK_UP, IN_PROGRESS).contains(entry.getKey()),
						toMap(Map.Entry::getKey, Map.Entry::getValue)));
		final ClientMonitoringData data = ImmutableClientMonitoringData.builder()
				.currentJobStatus(currentJobStatus)
				.jobStatusDurationMap(jobDurationMap)
				.isFinished(isFinished)
				.build();

		clientAgent.writeMonitoringData(CLIENT_MONITORING, data);
		updateJobDurationMapGUI();
	}

	public ClientJobStatusEnum getCurrentJobStatus() {
		return currentJobStatus;
	}

	public void setCurrentJobStatus(ClientJobStatusEnum currentJobStatus) {
		this.currentJobStatus = currentJobStatus;
	}

	public Timer getTimer() {
		return timer;
	}

	private void updateJobDurationMapGUI() {
		final List<ClientJobStatusEnum> simulationStatuses = List.of(SCHEDULED, PROCESSED, CREATED);
		final ToLongFunction<Map.Entry<ClientJobStatusEnum, Long>> getRealDuration = entry ->
				simulationStatuses.contains(entry.getKey()) ? entry.getValue() : convertToRealTime(entry.getValue());

		var guiJobDurationMap = getJobStatusDurationMap().entrySet().stream()
				.collect(toMap(Map.Entry::getKey, getRealDuration::applyAsLong));

		((ClientAgentNode) clientAgent.getAgentNode()).updateJobDurationMap(guiJobDurationMap);
	}

	private Map<ClientJobStatusEnum, Long> getJobStatusDurationMap() {
		if (clientAgent.isSplit()) {
			final Map<ClientJobStatusEnum, Long> result = new EnumMap<>(ClientJobStatusEnum.class);
			clientAgent.getJobParts().values().stream()
					.map(JobPart::getJobStatusDurationMap)
					.flatMap(map -> map.entrySet().stream()
							.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue())))
					.forEach(entry -> result.merge(entry.getKey(), entry.getValue(), Long::sum));
			return result;
		}
		return jobStatusDurationMap;
	}

	private boolean isOriginalStatusUpdated(final ClientJobStatusEnum status) {
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
