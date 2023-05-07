package com.database.knowledge.domain.agent;

import com.database.knowledge.domain.agent.client.ClientJobExecutionData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ClientStatisticsData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.domain.agent.monitoring.ProcessedApiRequest;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;

public enum DataType {

	DEFAULT(MonitoringData.class),
	CLIENT_MONITORING(ClientMonitoringData.class),
	CLIENT_STATISTICS(ClientStatisticsData.class),
	CLIENT_JOB_EXECUTION(ClientJobExecutionData.class),
	CLOUD_NETWORK_MONITORING(CloudNetworkMonitoringData.class),
	SERVER_MONITORING(ServerMonitoringData.class),
	GREEN_SOURCE_MONITORING(GreenSourceMonitoringData.class),
	AVAILABLE_GREEN_ENERGY(AvailableGreenEnergy.class),
	WEATHER_SHORTAGES(WeatherShortages.class),
	SHORTAGES(Shortages.class),
	HEALTH_CHECK(HealthCheck.class),
	PROCESSED_API_REQUEST(ProcessedApiRequest.class);

	private final Class<? extends MonitoringData> dataTypeClass;

	DataType(Class<? extends MonitoringData> dataTypeClass) {
		this.dataTypeClass = dataTypeClass;
	}

	public Class<? extends MonitoringData> getDataTypeClass() {
		return dataTypeClass;
	}
}
