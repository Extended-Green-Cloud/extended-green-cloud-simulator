package com.database.knowledge.domain.agent;

public record HealthCheck(boolean alive) implements MonitoringData {
}
