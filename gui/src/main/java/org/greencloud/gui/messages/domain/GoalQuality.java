package org.greencloud.gui.messages.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableGoalQuality.class)
@JsonDeserialize(as = ImmutableGoalQuality.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface GoalQuality {

	String getName();

	double getAvgQuality();
}
