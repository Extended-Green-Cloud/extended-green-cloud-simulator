package com.database.knowledge.domain.agent.greensource;

import java.sql.Timestamp;
import java.time.Instant;

import com.database.knowledge.domain.agent.MonitoringData;

/**
 * Record stores the information about energy supply decision made by the Green Source agent.
 *
 * @param windSpeed       wind speed in m/s of weather conditions at the moment of decision
 * @param temperature     temperature in Celsius of weather conditions at the moment of decision
 * @param radiation       solar radiation in W/m^2 of weather conditions at the moment of decision
 * @param greenSourceType type of green source used for energy supply
 * @param jobId           identifier of job for which the energy supply decision was made
 * @param toBeMigrated    flag indicating if the job is to be migrated to another server
 */
public record GreenEnergySupplyDecisionData(double windSpeed,
											double temperature,
											double radiation,
											String greenSourceType,
											String jobId,
											boolean toBeMigrated,
											Timestamp time) implements MonitoringData {
}
