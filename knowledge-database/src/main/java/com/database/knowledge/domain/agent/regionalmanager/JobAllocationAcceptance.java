package com.database.knowledge.domain.agent.regionalmanager;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Record to store the percentage ratio of accepted allocated jobs
 *
 * @param jobAllocationAcceptance percentage of allocated jobs that were accepted by the executors
 */
public record JobAllocationAcceptance(double jobAllocationAcceptance) implements MonitoringData {
}
