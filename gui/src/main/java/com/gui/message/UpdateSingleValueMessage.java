package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableUpdateSingleValueMessage.class)
@JsonDeserialize(as = ImmutableUpdateSingleValueMessage.class)
@Value.Immutable
public interface UpdateSingleValueMessage extends Message {

	double getData();
}
