package com.database.knowledge.domain.agent;

/**
 * Interface storing monitoring data common for network components
 */
public interface NetworkComponentMonitoringData extends MonitoringData {

	/**
	 * @return current aggregated success ratio
	 */
	double getSuccessRatio();
}
