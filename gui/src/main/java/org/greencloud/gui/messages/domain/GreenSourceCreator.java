package org.greencloud.gui.messages.domain;

import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableGreenSourceCreator.class)
@JsonDeserialize(as = ImmutableGreenSourceCreator.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface GreenSourceCreator extends EventData {

	String getName();

	String getServer();

	Location getLocation();

	Double getPricePerPowerUnit();

	Double getWeatherPredictionError();

	Long getMaximumCapacity();

	GreenEnergySourceTypeEnum getEnergyType();
}
