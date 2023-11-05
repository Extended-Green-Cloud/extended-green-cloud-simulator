package org.greencloud.commons.domain.cost;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data informing about price of job execution
 */
@JsonSerialize(as = ImmutableExecutionPrice.class)
@JsonDeserialize(as = ImmutableExecutionPrice.class)
@Value.Immutable
@ImmutableConfig
public interface ExecutionPrice {

	double getPrice();
}
