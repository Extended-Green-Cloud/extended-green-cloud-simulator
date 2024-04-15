package org.greencloud.gui.messages;

import org.jrba.environment.domain.ExternalMessage;
import org.greencloud.gui.messages.domain.WeatherDropData;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableWeatherDropMessage.class)
@JsonDeserialize(as = ImmutableWeatherDropMessage.class)
public interface WeatherDropMessage extends ExternalMessage {

	String getAgentName();

	WeatherDropData getData();
}
