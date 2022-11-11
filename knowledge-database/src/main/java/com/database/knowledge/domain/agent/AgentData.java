package com.database.knowledge.domain.agent;

import java.time.Instant;

public record AgentData(Instant timestamp, String aid, DataType dataType, MonitoringData monitoringData) {
}
