package com.database.knowledge.domain.agent.server;

import java.util.Map;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Record to store the current state of the server while processing job for execution.
 *
 * @param jobId                         job identifier
 * @param resourceUtilization           state of the server resources
 * @param resourceUtilizationPercentage percentage of utilized resources
 */
public record ServerProcessingData(String jobId,
								   Map<String, Double> resourceUtilization,
								   Map<String, Double> resourceUtilizationPercentage) implements MonitoringData {
}
