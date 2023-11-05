package org.greencloud.commons.domain.job.instance;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing data connecting given job instance with its execution price
 */
@JsonSerialize(as = ImmutableJobInstanceWithPrice.class)
@JsonDeserialize(as = ImmutableJobInstanceWithPrice.class)
@Value.Immutable
public interface JobInstanceWithPrice {

	/**
	 * @return unique identifier of job instance
	 */
	JobInstanceIdentifier getJobInstanceId();

	/**
	 * @return cost of job instance execution
	 */
	Double getPrice();
}
