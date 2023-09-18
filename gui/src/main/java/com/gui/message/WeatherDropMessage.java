package com.gui.message;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;
import com.gui.message.domain.WeatherDropData;

@Value.Immutable
@JsonSerialize(as = ImmutableWeatherDropMessage.class)
@JsonDeserialize(as = ImmutableWeatherDropMessage.class)
public interface WeatherDropMessage extends Message {

	String getAgentName();

	WeatherDropData getData();
}
