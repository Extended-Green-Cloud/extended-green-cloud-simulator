package com.greencloud.commons.args.event.powershortage;

import static com.greencloud.commons.args.event.EventTypeEnum.POWER_SHORTAGE_EVENT;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.event.EventArgs;
import com.greencloud.commons.exception.InvalidScenarioEventStructure;

/**
 * Interface containing properties of scenario event that triggers power shortage in specified agent
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePowerShortageEventArgs.class)
@JsonDeserialize(as = ImmutablePowerShortageEventArgs.class)
@JsonTypeName("POWER_SHORTAGE_EVENT")
public interface PowerShortageEventArgs extends EventArgs {

	/**
	 * @return name of the agent for which the event is triggered
	 */
	String getAgentName();

	/**
	 * @return flag informing if the event indicates power shortage start or finish
	 */
	boolean isFinished();

	/**
	 * @return cause of the power shortage (WEATHER_CAUSE, PHYSICAL_CAUSE)
	 */
	PowerShortageCause getCause();

	/**
	 * Method verifies the correctness of power shortage event structure
	 */
	@Override
	@Value.Check
	default void check() {
		EventArgs.super.check();

		if (!getType().equals(POWER_SHORTAGE_EVENT)) {
			throw new InvalidScenarioEventStructure(
					"Invalid event type. Acceptable event type is: POWER_SHORTAGE_EVENT");
		}
	}
}
