package org.greencloud.commons.args.adaptation.singleagent;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.adaptation.AdaptationActionParameters;

/**
 * Content of the message sent when the adaptation plan which changes Green Source connection with Server is
 * executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableChangeGreenSourceConnectionParameters.class)
@JsonSerialize(as = ImmutableChangeGreenSourceConnectionParameters.class)
public interface ChangeGreenSourceConnectionParameters extends AdaptationActionParameters {

	/**
	 * @return name of a Server to which the Green Source will be connected
	 */
	String getServerName();

	@Override
	default boolean dependsOnOtherAgents() {
		return true;
	}
}
