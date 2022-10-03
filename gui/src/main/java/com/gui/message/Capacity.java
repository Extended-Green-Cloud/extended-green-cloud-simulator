package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCapacity.class)
@JsonDeserialize(as = ImmutableCapacity.class)
@Value.Immutable
public interface Capacity {

	double getPowerInUse();

	double getMaximumCapacity();
}
