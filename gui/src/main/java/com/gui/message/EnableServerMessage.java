package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableEnableServerMessage.class)
@JsonDeserialize(as = ImmutableEnableServerMessage.class)
@Value.Immutable
public interface EnableServerMessage extends Message {

	String getServer();

	String getCna();

	double getCpu();

	default String getType() {
		return "ENABLE_SERVER";
	}
}
