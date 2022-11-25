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
	public double readCurrentGoalQuality(int time) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}
}
