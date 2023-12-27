package org.greencloud.strategyinjection.agentsystem.agents.restaurant.args;

import java.util.Map;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.strategyinjection.agentsystem.domain.CuisineType;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to construct restaurant agent
 */
@JsonSerialize(as = ImmutableRestaurantAgentArgs.class)
@JsonDeserialize(as = ImmutableRestaurantAgentArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface RestaurantAgentArgs extends AgentArgs {

	CuisineType getCuisineType();

	Map<String, Double> getDishWithPrice();

	Map<String, Object> getAdditionalInformation();

	/**
	 * Method returns agent arguments as object array
	 *
	 * @return object array
	 */
	@Override
	default Object[] getObjectArray() {
		return new Object[] { getCuisineType(), getDishWithPrice(), getAdditionalInformation() };
	}
}
