package com.greencloud.commons.args.agent.managing;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Objects;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.greencloud.commons.args.agent.AgentArgs;

@Value.Immutable
@JsonSerialize(as = ImmutableManagingAgentArgs.class)
@JsonDeserialize(as = ImmutableManagingAgentArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ManagingAgentArgs extends AgentArgs {

	/**
	 * @return threshold indicating the desired system quality
	 */
	double getSystemQualityThreshold();

	/**
	 * @return minimum number for power drops enabling green source to be considered
	 * in weather prediction error adaptation
	 */
	@Nullable
	Integer getPowerShortageThreshold();

	/**
	 * @return (optional) list of actions specified by their type names which are by default disabled in the system
	 */
	@Nullable
	@JacksonXmlElementWrapper(localName = "disabledActions")
	ArrayList<String> getDisabledActions();

	@Value.Check
	default void check() {
		if (getSystemQualityThreshold() <= 0 || getSystemQualityThreshold() > 1) {
			throw new InvalidParameterException("Quality threshold must be a value from range [0,1]");
		}
		if (Objects.nonNull(getPowerShortageThreshold()) && getPowerShortageThreshold() < 1) {
			throw new InvalidParameterException("Minimum number of power drops must be at least 1");
		}
	}
}
