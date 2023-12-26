package org.greencloud.commons.args.event;

import org.greencloud.commons.enums.agent.GreenEnergySourceTypeEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface containing properties of scenario event that generates new green source in Regional Manager
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNewGreenSourceCreationEventArgs.class)
@JsonDeserialize(as = ImmutableNewGreenSourceCreationEventArgs.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("GREEN_SOURCE_CREATION_EVENT")
public interface NewGreenSourceCreationEventArgs extends EventArgs {

	/**
	 * @return name of newly created agent
	 */
	String getName();

	/**
	 * @return name of the server with which the green source is to be connected
	 */
	String getServer();

	/**
	 * @return latitude of the location of green source
	 */
	Double getLatitude();

	/**
	 * @return longitude of the location of green source
	 */
	Double getLongitude();

	/**
	 * @return price of the single power unit of energy supply
	 */
	Double getPricePerPowerUnit();

	/**
	 * @return error associated with the weather predictions
	 */
	Double getWeatherPredictionError();

	/**
	 * @return maximum capacity of the green source
	 */
	Long getMaximumCapacity();

	/**
	 * @return type of the energy of green source
	 */
	GreenEnergySourceTypeEnum getEnergyType();
}
