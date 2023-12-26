package org.greencloud.commons.args.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.WEATHER_DROP_EVENT;

import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that triggers weather distortion in a given region
 */
@Value.Immutable
@JsonSerialize(as = ImmutableWeatherDropEventArgs.class)
@JsonDeserialize(as = ImmutableWeatherDropEventArgs.class)
@JsonTypeName("WEATHER_DROP_EVENT")
public interface WeatherDropEventArgs extends EventArgs {

	/**
	 * @return name of the regional manager for which region alert is triggeed
	 */
	String getAgentName();

	/**
	 * @return duration in hours (in real time) specifying how long the weather drop is going to last
	 */
	long getDuration();

	/**
	 * Method verifies the correctness of power shortage event structure
	 */
	@Override
	@Value.Check
	default void check() {
		EventArgs.super.check();

		if (!getType().equals(WEATHER_DROP_EVENT)) {
			throw new InvalidScenarioEventStructure(
					"Invalid event type. Acceptable event type is: WEATHER_DROP_EVENT");
		}
	}
}
