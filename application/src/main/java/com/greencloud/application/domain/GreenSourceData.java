package com.greencloud.application.domain;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object storing the data passed by the Green Source
 */
@JsonSerialize(as = ImmutableGreenSourceData.class)
@JsonDeserialize(as = ImmutableGreenSourceData.class)
@Immutable
public interface GreenSourceData {

	/**
	 * @return available power at the given time
	 */
	double getAvailablePowerInTime();

	/**
	 * @return price for the 1kWh
	 */
	double getPricePerPowerUnit();

	/**
	 * @return error associated with power calculations
	 */
	double getPowerPredictionError();

	/**
	 * @return unique identifier of the given job of interest
	 */
	String getJobId();
}
