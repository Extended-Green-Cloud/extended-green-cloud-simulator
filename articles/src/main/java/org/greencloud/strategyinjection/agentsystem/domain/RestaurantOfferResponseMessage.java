package org.greencloud.strategyinjection.agentsystem.domain;

import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableRestaurantOfferResponseMessage.class)
@JsonDeserialize(as = ImmutableRestaurantOfferResponseMessage.class)
@Value.Immutable
public interface RestaurantOfferResponseMessage extends Message {

	int getOrderId();

	boolean getAccepted();
}
