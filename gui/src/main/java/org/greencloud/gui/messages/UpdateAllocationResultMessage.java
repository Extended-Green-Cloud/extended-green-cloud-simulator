package org.greencloud.gui.messages;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateAllocationResultMessage.class)
@JsonDeserialize(as = ImmutableUpdateAllocationResultMessage.class)
@Value.Immutable
public interface UpdateAllocationResultMessage extends ExternalMessage {

	double getAllocationSuccess();

	double getAllocationTime();

	@Override
	default String getType() {
		return "UPDATE_ALLOCATION_RESULT";
	}
}
