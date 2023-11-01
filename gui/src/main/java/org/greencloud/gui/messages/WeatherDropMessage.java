package org.greencloud.gui.messages;

import org.greencloud.gui.messages.domain.Message;
import org.greencloud.gui.messages.domain.WeatherDropData;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableWeatherDropMessage.class)
@JsonDeserialize(as = ImmutableWeatherDropMessage.class)
public interface WeatherDropMessage extends Message {

	String getAgentName();

	WeatherDropData getData();
}
