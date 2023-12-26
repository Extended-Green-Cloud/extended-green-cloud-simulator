package org.greencloud.commons.args.adaptation.singleagent;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

/**
 * Content of the message sent when the adaptation plan which enables given Server is
 * executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableEnableServerActionParameters.class)
@JsonSerialize(as = ImmutableEnableServerActionParameters.class)
public interface EnableServerActionParameters extends AdaptationActionParameters {

	@Override
	default boolean dependsOnOtherAgents() {
		return true;
	}
}
