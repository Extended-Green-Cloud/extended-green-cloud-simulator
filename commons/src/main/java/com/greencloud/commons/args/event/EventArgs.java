package com.greencloud.commons.args.event;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.greencloud.commons.args.event.newclient.NewClientEventArgs;
import com.greencloud.commons.args.event.powershortage.PowerShortageEventArgs;
import com.greencloud.commons.exception.InvalidScenarioEventStructure;

/**
 * Generic interface containing common properties for defining scenario events
 */
@JsonTypeInfo(
		use = NAME,
		include = PROPERTY,
		property = "type",
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(name = "NEW_CLIENT_EVENT", value = NewClientEventArgs.class),
		@JsonSubTypes.Type(name = "POWER_SHORTAGE_EVENT", value = PowerShortageEventArgs.class)
})
public interface EventArgs {

	/**
	 * @return type of the event
	 */
	EventTypeEnum getType();

	/**
	 * @return number of seconds after which the event should be triggered
	 */
	Integer getOccurrenceTime();

	/**
	 * Method verifies the correctness of generic event structure
	 */
	@Value.Check
	default void check() {
		if (getOccurrenceTime() < 1) {
			throw new InvalidScenarioEventStructure(
					String.format("Occurrence time %d is invalid. The time should be greater than 1",
							getOccurrenceTime()));
		}
	}
}
