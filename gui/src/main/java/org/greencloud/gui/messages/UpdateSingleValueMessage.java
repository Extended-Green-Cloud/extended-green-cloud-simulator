package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateSingleValueMessage.class)
@JsonDeserialize(as = ImmutableUpdateSingleValueMessage.class)
@Value.Immutable
public interface UpdateSingleValueMessage extends Message {

	double getData();
}
