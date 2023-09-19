package org.greencloud.commons.domain.strategy;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object stores the data used in updating system strategy
 */
@JsonSerialize(as = ImmutableStrategyUpdate.class)
@JsonDeserialize(as = ImmutableStrategyUpdate.class)
@Value.Immutable
@ImmutableConfig
public interface StrategyUpdate {

	int getStrategyIdx();

	String getStrategyType();
}
