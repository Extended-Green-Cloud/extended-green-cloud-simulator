package org.greencloud.commons.domain.agent;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data passed by the Green Source
 */
@JsonSerialize(as = ImmutableGreenSourceData.class)
@JsonDeserialize(as = ImmutableGreenSourceData.class)
@Value.Immutable
@ImmutableConfig
public interface GreenSourceData {

	/**
	 * @return available power at the given time
	 */
	double getAvailablePowerInTime();

	/**
	 * @return price for supplying the energy for job execution
	 */
	double getPriceForEnergySupply();

	/**
	 * @return error associated with power calculations
	 */
	double getPowerPredictionError();

	/**
	 * @return unique identifier of the given job of interest
	 */
	String getJobId();
}
