package org.greencloud.commons.args.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.POWER_SHORTAGE_EVENT;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.enums.event.PowerShortageCauseEnum;

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
	PowerShortageCauseEnum getCause();

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
