package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateSingleValueMessage.class)
@JsonDeserialize(as = ImmutableUpdateSingleValueMessage.class)
@Value.Immutable
public interface UpdateSingleValueMessage extends ExternalMessage {

	double getData();
}
