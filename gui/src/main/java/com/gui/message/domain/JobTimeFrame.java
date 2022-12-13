package com.gui.message.domain;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.time.Instant;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonInclude(NON_NULL)
@JsonSerialize(as = ImmutableJobTimeFrame.class)
@JsonDeserialize(as = ImmutableJobTimeFrame.class)
public interface JobTimeFrame {

	Instant getStart();

	Instant getEnd();

	@Nullable
	String getSplitJobId();
}
