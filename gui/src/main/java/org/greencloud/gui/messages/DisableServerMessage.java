package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
