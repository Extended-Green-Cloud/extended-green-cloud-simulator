package org.greencloud.managingsystem.service.monitoring;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;

/**
 * Service containing methods connected with monitoring system's usage of backUp power
 */
public class BackUpPowerUsageService extends AbstractGoalService {

	public BackUpPowerUsageService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
	}

	@Override
	public double readCurrentGoalQuality(int time) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}
}
