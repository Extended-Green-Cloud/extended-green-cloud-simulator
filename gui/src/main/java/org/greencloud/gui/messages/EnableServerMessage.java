package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableEnableServerMessage.class)
@JsonDeserialize(as = ImmutableEnableServerMessage.class)
@Value.Immutable
public interface EnableServerMessage extends ExternalMessage {

	String getServer();

	String getRma();

	double getCpu();

	default String getType() {
		return "ENABLE_SERVER";
	}
}
