package com.database.knowledge.domain.goal;

public record AdaptationGoal(Integer id, String name, Double threshold, boolean isAboveThreshold, Double weight) {
}
