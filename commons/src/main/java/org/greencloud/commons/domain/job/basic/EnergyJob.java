package org.greencloud.commons.domain.job.basic;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.Var;

/**
 * Object storing the data describing job and estimated energy that may be required for its execution
 */
@JsonSerialize(as = ImmutableEnergyJob.class)
@JsonDeserialize(as = ImmutableEnergyJob.class)
@Value.Style(underrideHashCode = "hash", underrideEquals = "equalTo")
@Value.Immutable
@ImmutableConfig
public interface EnergyJob extends PowerJob {

	/**
	 * @return amount o energy required to power a given job (energy that needs to be provided per signle time unit)
	 */
	double getEnergy();

	@Override
	default int hash() {
		@Var int h = 5381;
		h += (h << 5) + getJobInstanceId().hashCode();
		return h;
	}

	default boolean equalTo(ImmutableEnergyJob another) {
		if (this == another)
			return true;
		return another != null && getJobInstanceId().equals(another.getJobInstanceId());
	}
}
