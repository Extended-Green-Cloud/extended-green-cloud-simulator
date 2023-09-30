package org.greencloud.strategyinjection.agentsystem.domain;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gui.message.domain.Message;

@JsonSerialize(as = ImmutableRestaurantLookUpMessage.class)
@JsonDeserialize(as = ImmutableRestaurantLookUpMessage.class)
@Value.Immutable
public interface RestaurantLookUpMessage extends Message {

	String getCuisine();

	String getDish();

	String getAdditionalInstructions();

	double getPrice();
}
