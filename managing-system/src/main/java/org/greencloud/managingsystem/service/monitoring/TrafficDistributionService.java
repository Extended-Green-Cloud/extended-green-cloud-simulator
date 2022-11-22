package org.greencloud.managingsystem.service.monitoring;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Service containing methods connected with monitoring system's traffic distribution
 */
public class TrafficDistributionService extends AbstractManagingService {

	private static final Logger logger = LoggerFactory.getLogger(TrafficDistributionService.class);

	private AtomicDouble averageTrafficDistribution;

	public TrafficDistributionService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.averageTrafficDistribution = new AtomicDouble(0);
	}

	public double getAverageTrafficDistribution() {
		return averageTrafficDistribution.get();
	}
}