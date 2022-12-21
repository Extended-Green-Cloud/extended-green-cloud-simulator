package com.database.knowledge.domain.agent.greensource;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Simple record to store the percentage ratio between available computed power and maximum capacity
 *
 * @param availablePowerPercentage percentage of available power computed from current weather data
 *                                 to current maximum capacity
 */
public record AvailableGreenEnergy(double availablePowerPercentage) implements MonitoringData {
}
