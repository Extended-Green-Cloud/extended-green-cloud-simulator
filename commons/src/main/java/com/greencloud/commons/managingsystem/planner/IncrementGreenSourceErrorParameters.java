package com.greencloud.commons.managingsystem.planner;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Content of the message sent when the adaptation plan which increases the Green Source
 * prediction error is to be executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableIncrementGreenSourceErrorParameters.class)
@JsonSerialize(as = ImmutableIncrementGreenSourceErrorParameters.class)
public interface IncrementGreenSourceErrorParameters extends AdaptationActionParameters {

	/**
	 * @return change which is to be applied on a given green source weather error indicator
	 */
	double getPercentageChange();

}
