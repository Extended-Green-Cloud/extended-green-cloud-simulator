package com.database.knowledge.domain.agent;

import java.time.Instant;

import com.database.knowledge.types.DataType;

public record AgentData(Instant timestamp, String aid, DataType dataType, MonitoringData monitoringData) {
}
