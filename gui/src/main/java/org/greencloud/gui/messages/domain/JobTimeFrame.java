package org.greencloud.gui.messages.domain;

import java.time.Instant;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableJobTimeFrame.class)
@JsonDeserialize(as = ImmutableJobTimeFrame.class)
public interface JobTimeFrame {

	Instant getStart();

	Instant getEnd();
}
