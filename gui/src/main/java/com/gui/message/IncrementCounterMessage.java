package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableIncrementCounterMessage.class)
@JsonDeserialize(as = ImmutableIncrementCounterMessage.class)
@Value.Immutable
public interface IncrementCounterMessage extends Message {
}
