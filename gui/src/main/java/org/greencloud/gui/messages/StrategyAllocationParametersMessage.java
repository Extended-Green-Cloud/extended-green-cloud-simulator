package org.greencloud.gui.messages;

import java.util.List;

import org.immutables.value.Value;
import org.jrba.environment.domain.ExternalMessage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableStrategyAllocationParametersMessage.class)
@JsonDeserialize(as = ImmutableStrategyAllocationParametersMessage.class)
@Value.Immutable
public interface StrategyAllocationParametersMessage extends ExternalMessage {

	String getAllocationName();

	String getPrioritizationName();

	Integer getAllocationStepsNo();

	List<String> getModificationList();

	default String getType() {
		return "UPDATE_STRATEGY_PARAMETERS";
	}
}
