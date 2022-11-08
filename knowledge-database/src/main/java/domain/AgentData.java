package domain;

import java.time.Instant;

public record AgentData(Instant timestamp, String aid, DataType dataType, MonitoringData monitoringData) {
}
