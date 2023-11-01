package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableIncrementCounterMessage.class)
@JsonDeserialize(as = ImmutableIncrementCounterMessage.class)
@Value.Immutable
public interface IncrementCounterMessage extends Message {
}
