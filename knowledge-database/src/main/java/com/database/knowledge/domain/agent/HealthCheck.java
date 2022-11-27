package com.database.knowledge.domain.agent;

import com.greencloud.commons.agent.AgentType;

public record HealthCheck(boolean alive, AgentType agentType) implements MonitoringData {
}
