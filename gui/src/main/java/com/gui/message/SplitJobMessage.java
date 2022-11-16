package com.gui.message;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.SplitJob;

@Value.Immutable
@JsonSerialize(as = ImmutableSplitJobMessage.class)
@JsonDeserialize(as = ImmutableSplitJobMessage.class)
public interface SplitJobMessage {

	String getJobId();

	List<SplitJob> getData();

	default String getType() {
		return "SPLIT_JOB";
	}
}
