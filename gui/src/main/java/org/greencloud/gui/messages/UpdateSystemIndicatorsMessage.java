package org.greencloud.gui.messages;

import java.util.Map;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableUpdateSystemIndicatorsMessage.class)
@JsonDeserialize(as = ImmutableUpdateSystemIndicatorsMessage.class)
@Value.Immutable
public interface UpdateSystemIndicatorsMessage extends Message {

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
