package org.greencloud.commons.domain.allocation;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Interface that encapsulates parameters used by aco-based allocator.
 */
@JsonSerialize(as = ImmutableAntAllocationParameters.class)
@JsonDeserialize(as = ImmutableAntAllocationParameters.class)
@Value.Immutable
public interface AntAllocationParameters {

	Integer getAntsNumber();

	Integer getMaxIterations();

	Double getAlphaWeight();

	Double getBetaWeight();

	Double getTrialDecay();

	Double getAdaptiveParam();
}
