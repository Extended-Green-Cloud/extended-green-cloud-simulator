package com.greencloud.commons.managingsystem.planner;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableDisableServerActionParameters.class)
@JsonSerialize(as = ImmutableDisableServerActionParameters.class)
public interface DisableServerActionParameters extends AdaptationActionParameters{
}
