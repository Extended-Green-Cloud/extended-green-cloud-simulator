package org.greencloud.commons.args.adaptation.singleagent;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Content of the message sent while executing the adaptation plan which increases the deadline priority
 * The content is empty, just a skeleton.
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableIncreaseJobDivisionPriorityParameters.class)
@JsonSerialize(as = ImmutableIncreaseJobDivisionPriorityParameters.class)
public interface IncreaseJobDivisionPriorityParameters extends AdaptationActionParameters {

}
