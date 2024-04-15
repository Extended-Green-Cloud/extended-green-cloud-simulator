package org.greencloud.gui.messages;

import java.util.Map;

import org.jrba.environment.domain.ExternalMessage;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateSystemIndicatorsMessage.class)
@JsonDeserialize(as = ImmutableUpdateSystemIndicatorsMessage.class)
@Value.Immutable
public interface UpdateSystemIndicatorsMessage extends ExternalMessage {

	/**
	 * @return quality indicator of the entire system
	 */
	double getSystemIndicator();

	/**
	 * @return map of goal identifiers and the corresponding current qualities
	 */
	Map<Integer, Double> getData();

	default String getType() {
		return "UPDATE_INDICATORS";
	}
}
