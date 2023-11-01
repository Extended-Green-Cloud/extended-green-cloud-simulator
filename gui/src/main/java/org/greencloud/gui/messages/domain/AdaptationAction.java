package org.greencloud.gui.messages.domain;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableAdaptationAction.class)
@JsonDeserialize(as = ImmutableAdaptationAction.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface AdaptationAction {

	String getName();

	String getGoal();

	int getRunsNo();

	List<GoalQuality> getAvgGoalQualities();

	double getAvgDuration();

}
