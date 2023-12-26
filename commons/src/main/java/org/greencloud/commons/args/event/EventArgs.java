package org.greencloud.commons.args.event;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import org.greencloud.commons.enums.event.EventTypeEnum;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Generic interface containing common properties for defining scenario events
 */
@JsonTypeInfo(
		use = NAME,
		include = PROPERTY,
		property = "type",
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(name = "CLIENT_CREATION_EVENT", value = NewClientEventArgs.class),
		@JsonSubTypes.Type(name = "DISABLE_SERVER_EVENT", value = DisableServerEventArgs.class),
		@JsonSubTypes.Type(name = "ENABLE_SERVER_EVENT", value = EnableServerEventArgs.class),
		@JsonSubTypes.Type(name = "MODIFY_RULE_SET", value = ModifyRuleSetEventArgs.class),
		@JsonSubTypes.Type(name = "SERVER_CREATION_EVENT", value = NewServerCreationEventArgs.class),
		@JsonSubTypes.Type(name = "GREEN_SOURCE_CREATION_EVENT", value = NewGreenSourceCreationEventArgs.class),
		@JsonSubTypes.Type(name = "WEATHER_DROP_EVENT", value = WeatherDropEventArgs.class),
		@JsonSubTypes.Type(name = "SERVER_MAINTENANCE_EVENT", value = ServerMaintenanceEventArgs.class),
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
