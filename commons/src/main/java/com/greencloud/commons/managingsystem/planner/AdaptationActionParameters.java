package com.greencloud.commons.managingsystem.planner;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Abstract interface prepared for the content of adaptation plans.
 * It has to be inherited by all other structures sent as content of the executor messages
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableAdaptationActionParameters.class)
@JsonSerialize(as = ImmutableAdaptationActionParameters.class)
public interface AdaptationActionParameters {
}
