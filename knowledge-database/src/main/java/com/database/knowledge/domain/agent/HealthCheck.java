package com.database.knowledge.domain.agent;

import org.greencloud.commons.args.agent.AgentType;

public record HealthCheck(boolean alive, AgentType agentType) implements MonitoringData {
}
