package org.greencloud.gui.messages.domain;

import java.time.Instant;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableEventData.class)
@JsonDeserialize(as = ImmutableEventData.class)
public interface EventData {

	Instant getOccurrenceTime();

	@Nullable
	Boolean isFinished();
}
