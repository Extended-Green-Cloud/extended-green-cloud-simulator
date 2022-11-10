package com.greencloud.application.agents.client;

import java.time.Instant;

import com.greencloud.application.agents.AbstractAgent;

import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Client Agent
 */
public abstract class AbstractClientAgent extends AbstractAgent {

	protected AID chosenCloudNetworkAgent;
	protected Instant simulatedJobStart;
	protected Instant simulatedJobEnd;

	protected Instant simulatedDeadline;
	protected Integer retries;
	protected boolean announced;

	protected AbstractClientAgent() {
		super.setup();
		retries = 0;
	}

	public void setChosenCloudNetworkAgent(AID chosenCloudNetworkAgent) {
		this.chosenCloudNetworkAgent = chosenCloudNetworkAgent;
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

	public Instant getSimulatedDeadline() { return simulatedDeadline; }

	public void setSimulatedDeadline(Instant simulatedDeadline) { this.simulatedDeadline = simulatedDeadline; }

	public Integer getRetries() {
		return retries;
	}

	public void retry() {
		retries++;
	}

	public void announce() {
		announced = true;
	}

	public boolean isAnnounced() {
		return announced;
	}
}
