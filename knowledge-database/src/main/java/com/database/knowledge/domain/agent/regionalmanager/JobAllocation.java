package com.database.knowledge.domain.agent.regionalmanager;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Record to store the percentage ratio of successfully allocated jobs
 *
 * @param jobAllocationPercentage percentage of successfully allocated jobs
 */
public record JobAllocation(double jobAllocationPercentage, long allocationTime) implements MonitoringData {
}
