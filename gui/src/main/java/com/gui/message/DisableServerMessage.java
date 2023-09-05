package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableDisableServerMessage.class)
@JsonDeserialize(as = ImmutableDisableServerMessage.class)
@Value.Immutable
public interface DisableServerMessage extends Message {

	String getServer();

	String getCna();

	double getCpu();

	default String getType() {
		return "DISABLE_SERVER";
	}
}
