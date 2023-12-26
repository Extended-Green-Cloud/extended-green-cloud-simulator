package org.greencloud.commons.args.agent.server.factory;

import static java.util.Objects.isNull;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;

import java.security.InvalidParameterException;
import java.util.Map;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Server Agent
 */
@JsonSerialize(as = ImmutableServerArgs.class)
@JsonDeserialize(as = ImmutableServerArgs.class)
@Immutable
public interface ServerArgs extends AgentArgs {

	/**
	 * @return owner cloud network agent name
	 */
	String getOwnerRegionalManager();

	/**
	 * @return maximum server power consumption (i.e. when CPU load is 100%)
	 */
	Integer getMaxPower();

	/**
	 * @return idle power consumption
	 */
	Integer getIdlePower();

	/**
	 * @return amount of hardware resources owned by server
	 */
	Map<String, Resource> getResources();

	/**
	 * @return limit of jobs that can be processed at the same time
	 */
	Integer getJobProcessingLimit();

	/**
	 * @return price per 1-hour
	 */
	Double getPrice();

	@Nullable
	String getContainerId();

	@Value.Check
	default void check() {
		if (isNull(getOwnerRegionalManager())) {
			throw new InvalidParameterException("Owner RMA should be specified.");
		}
		if (isNull(getResources())) {
			throw new InvalidParameterException("Hardware resources must be specified.");
		}
		if (isNull(getMaxPower()) && getMaxPower() >= 0) {
			throw new InvalidParameterException("Maximum power consumption of the server cannot be smaller than 0.");
		}
		if (isNull(getIdlePower()) && getIdlePower() >= 0) {
			throw new InvalidParameterException("Idle power consumption of the server cannot be smaller than 0.");
		}
		if (isNull(getPrice()) && getPrice() >= 0) {
			throw new InvalidParameterException("Price per power unit cannot be smaller than 0.");
		}
		if (!getResources().containsKey(CPU)) {
			throw new InvalidParameterException("Each server must have CPU value specified.");
		}
		if (isNull(getJobProcessingLimit()) && getJobProcessingLimit() > 0) {
			throw new InvalidParameterException("Job processing limit must be greater than 0.");
		}
	}

}
