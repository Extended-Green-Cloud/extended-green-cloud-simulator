package com.greencloud.commons.args.agent.managing;

import java.security.InvalidParameterException;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

@Value.Immutable
@JsonSerialize(as = ImmutableManagingAgentArgs.class)
@JsonDeserialize(as = ImmutableManagingAgentArgs.class)
public interface ManagingAgentArgs extends AgentArgs {

	/**
	 * @return threshold indicating the desired system quality
	 */
	double getSystemQualityThreshold();

	@Value.Check
	default void check() {
		if (getSystemQualityThreshold() <= 0 || getSystemQualityThreshold() > 1) {
			throw new InvalidParameterException("Quality threshold must be a value from range [0,1]");
		}
	}
}
