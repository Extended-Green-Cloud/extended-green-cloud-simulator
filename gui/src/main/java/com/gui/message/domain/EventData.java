package com.gui.message.domain;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableEventData.class)
@JsonDeserialize(as = ImmutableEventData.class)
public interface EventData {

	Instant getOccurrenceTime();

	boolean isFinished();
}
