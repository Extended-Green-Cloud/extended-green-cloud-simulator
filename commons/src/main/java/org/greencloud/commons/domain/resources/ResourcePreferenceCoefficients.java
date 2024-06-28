package org.greencloud.commons.domain.resources;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class represents preferences set for resources
 *
 * @implNote it follows coefficient needed for algorithm
 * [<a href="https://www.sciencedirect.com/science/article/pii/S0020025520304588">Intent-based allocation</a>]
 */
@JsonSerialize(as = ImmutableResourcePreferenceCoefficients.class)
@JsonDeserialize(as = ImmutableResourcePreferenceCoefficients.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Value.Immutable
public interface ResourcePreferenceCoefficients {

	/**
	 * @return importance of CPU utilization for resource performance
	 */
	Double getCpuExperienceCoefficient();

	/**
	 * @return importance of MEMORY utilization for resource performance
	 */
	Double getMemoryExperienceCoefficient();

	/**
	 * @return importance of STORAGE utilization for resource performance
	 */
	Double getStorageExperienceCoefficient();

	/**
	 * @return preference set towards matching with respect to job execution cost
	 */
	Double getCostWeights();

	/**
	 * @return preference set towards matching with respect to resource reliability
	 */
	Double getReliabilityWeight();

	/**
	 * @return preference set towards matching with respect to energy utilization
	 */
	Double getEnergyWeight();

	/**
	 * @return preference set towards matching with respect to job execution time
	 */
	Double getTimeWeight();

	/**
	 * @return preference set towards matching with respect to resource performance
	 */
	Double getPerformanceWeight();

	/**
	 * @return weight put on job satisfaction
	 */
	Double jobSatisfactionWeight();

	/**
	 * @return weight put on executor satisfaction
	 */
	Double executorSatisfactionWeight();

	/**
	 * @return minimal satisfaction of the job
	 */
	Double getMinimalJobSatisfaction();

	/**
	 * @return required minimal satisfaction of the executor
	 */
	Double getMinimalExecutorSatisfaction();
}
