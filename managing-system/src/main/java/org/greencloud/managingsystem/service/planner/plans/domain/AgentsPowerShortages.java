package org.greencloud.managingsystem.service.planner.plans.domain;

/**
 * Record describing count of the power shortages for corresponding agent
 *
 * @param name  agent name
 * @param value power shortages count
 */
public record AgentsPowerShortages(String name, Integer value) {
}
