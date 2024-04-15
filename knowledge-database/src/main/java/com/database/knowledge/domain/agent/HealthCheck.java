package com.database.knowledge.domain.agent;

import org.greencloud.commons.args.agent.EGCSAgentType;

public record HealthCheck(boolean alive, EGCSAgentType agentType) implements MonitoringData {
}
