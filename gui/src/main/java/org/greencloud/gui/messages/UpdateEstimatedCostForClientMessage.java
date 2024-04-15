package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateEstimatedCostForClientMessage.class)
@JsonDeserialize(as = ImmutableUpdateEstimatedCostForClientMessage.class)
@Value.Immutable
public interface UpdateEstimatedCostForClientMessage extends ExternalMessage {

	/**
	 * @return name of the client agent
	 */
	String getAgentName();

	/**
	 * @return estimated job execution price
	 */
	Double getEstimatedPrice();

	default String getType() {
		return "UPDATE_ESTIMATED_COST_FOR_CLIENT";
	}
}
