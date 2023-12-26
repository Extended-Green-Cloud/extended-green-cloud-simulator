package org.greencloud.commons.args.adaptation.singleagent;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Content of the message sent when the adaptation plan which disables given Server is
 * executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableDisableServerActionParameters.class)
@JsonSerialize(as = ImmutableDisableServerActionParameters.class)
public interface DisableServerActionParameters extends AdaptationActionParameters {

	@Override
	default boolean dependsOnOtherAgents() {
		return true;
	}
}
