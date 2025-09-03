package org.greencloud.commons.domain.adaptation.goal;

/**
 * Object representing adaptation goal.
 *
 * @param id               goal identifier
 * @param name             name of the adaptation goal
 * @param threshold        threshold that needs to be fulfilled
 * @param isAboveThreshold flag indicating whether the goal is fulfilled if it goes beyond or below the threshold
 * @param weight           weight (aka. importance) of adaptation goal
 */
public record AdaptationGoal(Integer id, String name, Double threshold, boolean isAboveThreshold, Double weight) {
}
