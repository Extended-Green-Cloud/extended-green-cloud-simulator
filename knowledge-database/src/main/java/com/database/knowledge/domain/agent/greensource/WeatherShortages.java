package com.database.knowledge.domain.agent.greensource;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Simple record to store the number of weather shortages in the given span of time.
 *
 * @param weatherShortagesNumber how many weather shortages has happened
 * @param timePeriod             time period in which the number was collected
 */
public record WeatherShortages(int weatherShortagesNumber, long timePeriod) implements MonitoringData {
}
