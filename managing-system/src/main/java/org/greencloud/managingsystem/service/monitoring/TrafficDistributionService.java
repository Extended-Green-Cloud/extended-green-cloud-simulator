package org.greencloud.managingsystem.service.monitoring;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractGoalService {

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public boolean evaluateAndUpdate() {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		//TODO ADD IMPLEMENTATION HERE
		return 0.0;
	}
}
