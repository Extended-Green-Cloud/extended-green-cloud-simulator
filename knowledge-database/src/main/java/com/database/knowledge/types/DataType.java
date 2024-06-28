package com.database.knowledge.types;

import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.domain.agent.client.ClientJobExecutionData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ClientStatisticsData;
import com.database.knowledge.domain.agent.regionalmanager.JobAllocation;
import com.database.knowledge.domain.agent.regionalmanager.JobAllocationAcceptance;
import com.database.knowledge.domain.agent.regionalmanager.RegionalManagerMonitoringData;
import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.monitoring.ProcessedApiRequest;
import com.database.knowledge.domain.agent.server.JobEnergyUtilization;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataType {

	DEFAULT(MonitoringData.class),
	CLIENT_MONITORING(ClientMonitoringData.class),
	CLIENT_STATISTICS(ClientStatisticsData.class),
	CLIENT_JOB_EXECUTION(ClientJobExecutionData.class),
	REGIONAL_MANAGER_MONITORING(RegionalManagerMonitoringData.class),
	SERVER_MONITORING(ServerMonitoringData.class),
	JOB_ENERGY_UTILIZATION(JobEnergyUtilization.class),
	JOB_ALLOCATION(JobAllocation.class),
	JOB_ALLOCATION_ACCEPTANCE(JobAllocationAcceptance.class),
	GREEN_SOURCE_MONITORING(GreenSourceMonitoringData.class),
	AVAILABLE_GREEN_ENERGY(AvailableGreenEnergy.class),
	WEATHER_SHORTAGES(WeatherShortages.class),
	SHORTAGES(Shortages.class),
	HEALTH_CHECK(HealthCheck.class),
	PROCESSED_API_REQUEST(ProcessedApiRequest.class);

	private final Class<? extends MonitoringData> dataTypeClass;
}
