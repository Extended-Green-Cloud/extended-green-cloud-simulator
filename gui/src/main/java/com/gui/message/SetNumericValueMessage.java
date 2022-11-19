package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableSetNumericValueMessage.class)
@JsonDeserialize(as = ImmutableSetNumericValueMessage.class)
@Value.Immutable
public interface SetNumericValueMessage extends Message {

	double getData();

	String getAgentName();
}
