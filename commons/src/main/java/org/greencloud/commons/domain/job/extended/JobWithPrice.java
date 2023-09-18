package org.greencloud.commons.domain.job.extended;

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
}
