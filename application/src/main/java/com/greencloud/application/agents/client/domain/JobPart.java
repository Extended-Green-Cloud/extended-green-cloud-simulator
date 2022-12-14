package com.greencloud.application.agents.client.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.greencloud.application.utils.domain.Timer;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ClientJobStatusEnum;

/**
 * POJO representing a part of job created after job splitting. It is used to track changes in the state of a part of
 * job within the client agent.
 */
public class JobPart {

	private final ClientJob job;
	private ClientJobStatusEnum status;
	protected Map<ClientJobStatusEnum, Long> jobStatusDurationMap;
	private Instant simulatedJobStart;
	private Instant simulatedJobEnd;
	private Instant simulatedDeadline;
	protected final Timer timer = new Timer();

	public JobPart(ClientJob job, ClientJobStatusEnum status, Instant simulatedJobStart, Instant simulatedJobEnd,
				   Instant simulatedDeadline) {
		this.job = job;
		this.status = status;
		this.simulatedJobStart = simulatedJobStart;
		this.simulatedJobEnd = simulatedJobEnd;
		this.simulatedDeadline = simulatedDeadline;
		jobStatusDurationMap = Arrays.stream(ClientJobStatusEnum.values())
				.collect(Collectors.toMap(statusEnum -> statusEnum, statusEnum -> 0L));
		timer.startTimeMeasure();
	}

	public ClientJob getJob() {
		return job;
	}

	public ClientJobStatusEnum getStatus() {
		return status;
	}

	public void setStatus(ClientJobStatusEnum status) {
		this.status = status;
	}

	public Instant getSimulatedJobStart() {
		return simulatedJobStart;
	}

	public void setSimulatedJobStart(Instant simulatedJobStart) {
		this.simulatedJobStart = simulatedJobStart;
	}

	public Instant getSimulatedJobEnd() {
		return simulatedJobEnd;
	}

	public void setSimulatedJobEnd(Instant simulatedJobEnd) {
		this.simulatedJobEnd = simulatedJobEnd;
	}

	public Instant getSimulatedDeadline() {
		return simulatedDeadline;
	}

	public Map<ClientJobStatusEnum, Long> getJobStatusDurationMap() {
		return jobStatusDurationMap;
	}

	public synchronized void updateJobStatusDuration(final ClientJobStatusEnum newStatus) {
		final long elapsedTime = timer.stopTimeMeasure();
		timer.startTimeMeasure();
		jobStatusDurationMap.computeIfPresent(status, (key, val) -> val + elapsedTime);
		status = newStatus;
	}
}
