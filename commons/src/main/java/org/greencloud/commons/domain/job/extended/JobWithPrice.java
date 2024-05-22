package org.greencloud.commons.domain.job.extended;

import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.energy.EnergyTypeEnum;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
	@Nullable
	Double getPriceForJob();

	/**
	 * @return estimated job execution time
	 */
	@Nullable
	Double getExecutionTime();

	/**
	 * @return type of energy with which a given job is to be executed
	 */
	@Nullable
	EnergyTypeEnum getTypeOfEnergy();

	/**
	 * @return resources available for job execution
	 */
	@Nullable
	Map<String, Resource> getAvailableResources();
}
