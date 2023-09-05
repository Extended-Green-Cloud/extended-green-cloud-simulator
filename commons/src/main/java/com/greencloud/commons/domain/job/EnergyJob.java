package com.greencloud.commons.domain.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data describing job and estimated energy that may be required for its execution
 */
@JsonSerialize(as = ImmutableEnergyJob.class)
@JsonDeserialize(as = ImmutableEnergyJob.class)
@Value.Immutable
@ImmutableConfig
public interface EnergyJob extends PowerJob {

	/**
	 * @return amount o energy required to power a given job (energy that needs to be provided per signle time unit)
	 */
	double getEnergy();
}
