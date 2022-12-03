package com.greencloud.commons.managingsystem.planner;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * content of the message sent while executing the adaptation plan which increases the deadline priority
 * The content is empty, just a skeleton.
 */
@Value.Immutable
@JsonDeserialize(as = IncreaseJobDivisionPriorityParameters.class)
@JsonSerialize(as = IncreaseJobDivisionPriorityParameters.class)
public interface IncreaseJobDivisionPriorityParameters extends AdaptationActionParameters {

}
