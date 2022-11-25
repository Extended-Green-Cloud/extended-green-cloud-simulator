package com.database.knowledge.domain.systemquality;

import java.time.Instant;

public record SystemQuality(Instant timestamp, Integer goalId, Double quality) {
}
