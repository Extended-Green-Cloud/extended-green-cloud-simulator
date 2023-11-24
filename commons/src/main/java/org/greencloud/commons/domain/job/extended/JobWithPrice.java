package org.greencloud.commons.domain.job.extended;

import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.enums.energy.EnergyTypeEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data describing job and the cost of its execution
 */
@JsonSerialize(as = ImmutableJobWithPrice.class)
@JsonDeserialize(as = ImmutableJobWithPrice.class)
@Value.Immutable
@ImmutableConfig
public interface JobWithPrice {

	/**
	 * @return unique identifier of the given job
	 */
	String getJobId();

	/**
	 * @return cost of execution of the given job
	 */
	double getPriceForJob();

	/**
	 * @return type of energy with which a given job is to be executed
	 */
	EnergyTypeEnum getTypeOfEnergy();

	/**
	 * @return specification of the server that will execute given job
	 */
	ServerResources getServerResources();
}
