package com.gui.message.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutablePowerShortageData.class)
@JsonDeserialize(as = ImmutablePowerShortageData.class)
public interface PowerShortageData extends EventData {

	Double getNewMaximumCapacity();

}
