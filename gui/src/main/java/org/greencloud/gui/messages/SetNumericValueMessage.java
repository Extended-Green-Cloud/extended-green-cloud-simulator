package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableSetNumericValueMessage.class)
@JsonDeserialize(as = ImmutableSetNumericValueMessage.class)
@Value.Immutable
public interface SetNumericValueMessage extends Message {

	double getData();

	String getAgentName();
}
