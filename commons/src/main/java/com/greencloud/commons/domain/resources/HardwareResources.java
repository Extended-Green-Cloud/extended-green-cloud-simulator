package com.greencloud.commons.domain.resources;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.domain.ImmutableConfig;

/**
Class describing hardware resources of server
 */
@JsonSerialize(as = ImmutableHardwareResources.class)
@JsonDeserialize(as = ImmutableHardwareResources.class)
@Value.Immutable
@ImmutableConfig
public interface HardwareResources {

	/**
	 * @return number of CPU cores
	 */
	Double getCpu();

	/**
	 * @return server memory (in Gi)
	 */
	Double getMemory();

	/**
	 * @return server SSD storage (in Gi)
	 */
	Double getStorage();

	/**
	 * Method subtracts from the resources, the resources given as an argument.
	 *
	 * @param hardwareResources resources that are to be subtracted
	 * @return difference between resources
	 */
	default HardwareResources computeResourceDifference(final HardwareResources hardwareResources) {
		return ImmutableHardwareResources.builder()
				.cpu(getCpu() - hardwareResources.getCpu())
				.memory(getMemory() - hardwareResources.getMemory())
				.storage(getStorage() - hardwareResources.getStorage())
				.build();
	}

	/**
	 * Method returns information if the resource amount is sufficient with regard to given required amount.
	 *
	 * @param requiredResources required amount of resource
	 * @return boolean indicating if resource amount is sufficient
	 */
	default boolean areSufficient(final HardwareResources requiredResources) {
		return getCpu() >= requiredResources.getCpu()
				&& getMemory() >= requiredResources.getMemory()
				&& getStorage() >= requiredResources.getStorage();
	}
}
