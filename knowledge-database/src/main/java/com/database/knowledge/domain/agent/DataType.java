package com.database.knowledge.domain.agent;

import com.database.knowledge.domain.agent.monitoring.ProcessedApiRequest;

public enum DataType {

	DEFAULT(MonitoringData.class),
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
