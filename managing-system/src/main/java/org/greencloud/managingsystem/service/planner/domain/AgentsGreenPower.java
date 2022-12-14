package org.greencloud.managingsystem.service.planner.domain;

/**
 * Record describing corresponding to given agent green power value
 * @param name agent name
 * @param value green power value
 */
public record AgentsGreenPower(String name, Double value) {
}
