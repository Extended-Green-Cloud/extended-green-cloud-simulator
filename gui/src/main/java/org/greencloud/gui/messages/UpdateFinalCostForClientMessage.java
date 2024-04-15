package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateFinalCostForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateFinalCostForClientMessage.class)
@Value.Immutable
public interface UpdateFinalCostForClientMessage extends ExternalMessage {

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
