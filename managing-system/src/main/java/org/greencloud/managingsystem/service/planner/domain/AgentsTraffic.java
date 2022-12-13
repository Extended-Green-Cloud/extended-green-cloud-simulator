package org.greencloud.managingsystem.service.planner.domain;

/**
 * Record describing corresponding to given agent traffic value
 * @param name agent name
 * @param value traffic value
 */
public record AgentsTraffic(String name, Double value) {
}
