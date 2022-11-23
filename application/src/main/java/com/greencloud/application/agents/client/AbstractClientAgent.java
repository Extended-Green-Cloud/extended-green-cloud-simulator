package com.greencloud.application.agents.client;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.agents.client.management.ClientStateManagement;
import com.greencloud.commons.job.ClientJob;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

	protected ClientJob myJob;
	protected Instant simulatedJobStart;
	protected Instant simulatedJobEnd;
	protected Instant simulatedDeadline;
	protected boolean announced;
	protected boolean split;
	protected Map<String, JobPart> jobParts;
	protected ClientStateManagement clientStateManagement;

	protected AbstractClientAgent() {
		super.setup();
		jobParts = new HashMap<>();
	}

	public ClientJob getMyJob() {
		return myJob;
	}

	public void setMyJob(ClientJob myJob) {
		this.myJob = myJob;
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

	public ClientStateManagement manage() {
		return clientStateManagement;
	}
}
