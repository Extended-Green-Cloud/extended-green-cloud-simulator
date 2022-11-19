package com.greencloud.application.agents.client.domain;

import java.time.Instant;

import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.JobStatusEnum;

/**
 * POJO representing a part of job created after job splitting. It is used to track changes in the state of a part of
 * job within the client agent.
 */
public class JobPart {

	private final ClientJob job;
	private JobStatusEnum status;
	private Instant simulatedJobStart;
	private Instant simulatedJobEnd;
	private Instant simulatedDeadline;

	public JobPart(ClientJob job, JobStatusEnum status, Instant simulatedJobStart, Instant simulatedJobEnd,
			Instant simulatedDeadline) {
		this.job = job;
		this.status = status;
		this.simulatedJobStart = simulatedJobStart;
		this.simulatedJobEnd = simulatedJobEnd;
		this.simulatedDeadline = simulatedDeadline;
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

	public void setSimulatedDeadline(Instant simulatedDeadline) {
		this.simulatedDeadline = simulatedDeadline;
	}
}
