package org.greencloud.commons.domain.agent;

import java.util.Map;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data regarding jobs' prices estimation
 */
@JsonSerialize(as = ImmutableServerPriceEstimation.class)
@JsonDeserialize(as = ImmutableServerPriceEstimation.class)
@Value.Immutable
@ImmutableConfig
public interface ServerPriceEstimation {

	/**
	 * @return estimation of jobs' prices
	 */
	Map<String, Double> getJobsPrices();

	/**
	 * @return average green energy utilization
	 */
	@Nullable
	Double getAverageGreenEnergyUtilization();
}
