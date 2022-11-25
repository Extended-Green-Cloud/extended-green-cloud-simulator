package com.gui.message.domain;

import java.time.Instant;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.database.knowledge.domain.action.AdaptationActionTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableAdaptationLog.class)
@JsonDeserialize(as = ImmutableAdaptationLog.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface AdaptationLog {

	/**
	 * @return type of the adaptation action
	 */
	AdaptationActionTypeEnum getType();

	/**
	 * @return description of performed adaptation
	 */
	String getDescription();

	/**
	 * @return optional name of the agent on which the adaptation was performed
	 */
	@Nullable
	String getAgentName();

	/**
	 * @return time when the adaptation was performed
	 */
	Instant getTime();
}
