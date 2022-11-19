package com.greencloud.application.agents.client;

import static com.greencloud.commons.job.JobStatusEnum.CREATED;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.JobStatusEnum;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

	protected ClientJob myJob;
	protected JobStatusEnum currentJobStatus;
	protected Instant simulatedJobStart;
	protected Instant simulatedJobEnd;
	protected Instant simulatedDeadline;
	protected boolean announced;
	protected boolean split;
	protected Map<String, JobPart> jobParts;

	protected AbstractClientAgent() {
		super.setup();
		currentJobStatus = CREATED;
		jobParts = new HashMap<>();
	}

	public void setMyJob(ClientJob myJob) {
		this.myJob = myJob;
	}

	public ClientJob getMyJob() {
		return myJob;
	}

	public JobStatusEnum getCurrentJobStatus() {
		return currentJobStatus;
	}

	public void setCurrentJobStatus(JobStatusEnum currentJobStatus) {
		this.currentJobStatus = currentJobStatus;
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

	public void announce() {
		announced = true;
	}

	public boolean isAnnounced() {
		return announced;
	}

	public boolean isSplit() {
		return split;
	}

	public void split() {
		split = true;
	}

	public Map<String, JobPart> getJobParts() {
		return jobParts;
	}
}
