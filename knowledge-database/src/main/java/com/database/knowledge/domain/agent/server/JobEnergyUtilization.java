package com.database.knowledge.domain.agent.server;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Record to store the percentage ratio of green energy utilization
 *
 * @param greenEnergyUtilization percentage of green energy utilization to total energy utilized for job execution
 */
public record JobEnergyUtilization(double greenEnergyUtilization) implements MonitoringData {
}
