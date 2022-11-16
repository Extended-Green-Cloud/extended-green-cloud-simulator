package com.gui.message.domain;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonInclude(NON_NULL)
@JsonSerialize(as = ImmutableJobStatus.class)
@JsonDeserialize(as = ImmutableJobStatus.class)
public interface JobStatus {

	String getStatus();

	@Nullable
	String getSplitJobId();
}
