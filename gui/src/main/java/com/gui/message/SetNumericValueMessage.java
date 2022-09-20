package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetNumericValueMessage.class)
@JsonDeserialize(as = ImmutableSetNumericValueMessage.class)
@Value.Immutable
public interface SetNumericValueMessage {

	double getData();

	String getAgentName();

	String getType();
}
