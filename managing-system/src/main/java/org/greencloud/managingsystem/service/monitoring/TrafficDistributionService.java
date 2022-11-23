package org.greencloud.managingsystem.service.monitoring;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractManagingService {

	private final AtomicDouble averageTrafficDistribution;

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.averageTrafficDistribution = new AtomicDouble(0);
	}

	public double getAverageTrafficDistribution() {
		return averageTrafficDistribution.get();
	}
}