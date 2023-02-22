package org.greencloud.managingsystem.service.planner.plans.domain;

/**
 * Record describing back up power usage for corresponding agent
 *
 * @param name  agent name
 * @param value used back up power
 */
public record AgentsBackUpPower(String name, Double value) {
}
