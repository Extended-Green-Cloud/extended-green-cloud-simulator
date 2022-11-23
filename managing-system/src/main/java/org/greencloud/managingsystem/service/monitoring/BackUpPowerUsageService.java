package org.greencloud.managingsystem.service.monitoring;

import org.greencloud.managingsystem.agent.AbstractManagingAgent;
import org.greencloud.managingsystem.service.AbstractManagingService;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Service containing methods connected with monitoring system's usage of backUp power
 */
public class BackUpPowerUsageService extends AbstractManagingService {

	private final AtomicDouble backUpPowerUsage;

	public BackUpPowerUsageService(AbstractManagingAgent managingAgent) {
		super(managingAgent);
		this.backUpPowerUsage = new AtomicDouble(0);
	}

	public double getBackUpPowerUsage() {
		return backUpPowerUsage.get();
	}
}
