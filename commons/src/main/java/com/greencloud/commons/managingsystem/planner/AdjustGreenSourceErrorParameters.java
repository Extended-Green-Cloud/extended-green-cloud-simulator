package com.greencloud.commons.managingsystem.planner;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Content of the message sent when the adaptation plan which increases the Green Source
 * prediction error is to be executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableAdjustGreenSourceErrorParameters.class)
@JsonSerialize(as = ImmutableAdjustGreenSourceErrorParameters.class)
public interface AdjustGreenSourceErrorParameters extends AdaptationActionParameters {

	/**
	 * @return number by which the error percentage is to be changed
	 */
	double getPercentageChange();
}
