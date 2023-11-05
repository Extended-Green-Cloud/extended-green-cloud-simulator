package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateFinalCostForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateFinalCostForClientMessage.class)
@Value.Immutable
public interface UpdateFinalCostForClientMessage extends Message {

	/**
	 * @return name of the client agent
	 */
	String getAgentName();

	/**
	 * @return final job execution price
	 */
	Double getFinalPrice();

	default String getType() {
		return "UPDATE_FINAL_COST_FOR_CLIENT";
	}
}
