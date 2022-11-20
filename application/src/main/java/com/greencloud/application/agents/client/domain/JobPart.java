package com.greencloud.application.agents.client.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.greencloud.application.utils.domain.Timer;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.JobStatusEnum;

/**
 * POJO representing a part of job created after job splitting. It is used to track changes in the state of a part of
 * job within the client agent.
 */
public class JobPart {

	private final ClientJob job;
	private JobStatusEnum status;
	protected Map<JobStatusEnum, Long> jobStatusDurationMap;
	private Instant simulatedJobStart;
	private Instant simulatedJobEnd;
	private final Instant simulatedDeadline;
	protected final Timer timer = new Timer();

	public JobPart(ClientJob job, JobStatusEnum status, Instant simulatedJobStart, Instant simulatedJobEnd,
			Instant simulatedDeadline) {
		this.job = job;
		this.status = status;
		this.simulatedJobStart = simulatedJobStart;
		this.simulatedJobEnd = simulatedJobEnd;
		this.simulatedDeadline = simulatedDeadline;
		jobStatusDurationMap = Arrays.stream(JobStatusEnum.values())
				.collect(Collectors.toMap(statusEnum -> statusEnum, statusEnum -> 0L));
		timer.startTimeMeasure();
	}

	public ClientJob getJob() {
		return job;
	}

	public JobStatusEnum getStatus() {
		return status;
	}

	public void setStatus(JobStatusEnum status) {
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

	public Map<JobStatusEnum, Long> getJobStatusDurationMap() {
		return jobStatusDurationMap;
	}

	public synchronized void updateJobStatusDuration(final JobStatusEnum newStatus) {
		final long elapsedTime = timer.stopTimeMeasure();
		timer.startTimeMeasure();
		jobStatusDurationMap.computeIfPresent(status, (key, val) -> val + elapsedTime);
		status = newStatus;
	}
}
