package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableDisableServerMessage.class)
@JsonDeserialize(as = ImmutableDisableServerMessage.class)
@Value.Immutable
public interface DisableServerMessage extends ExternalMessage {

	String getServer();

	String getRma();

	double getCpu();

	default String getType() {
		return "DISABLE_SERVER";
	}
}
