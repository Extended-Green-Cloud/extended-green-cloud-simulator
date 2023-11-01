package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
