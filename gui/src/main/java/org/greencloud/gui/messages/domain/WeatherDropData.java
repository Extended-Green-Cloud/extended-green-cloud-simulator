package org.greencloud.gui.messages.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableWeatherDropData.class)
@JsonDeserialize(as = ImmutableWeatherDropData.class)
public interface WeatherDropData extends EventData {

	long getDuration();
}
