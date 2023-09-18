package org.greencloud.commons.domain.job.transfer;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.job.basic.PowerJob;

/**
 * Object stores the data representing given job divided into 2 instances
 */
@JsonSerialize(as = ImmutableJobDivided.class)
@JsonDeserialize(as = ImmutableJobDivided.class)
@Value.Immutable
@ImmutableConfig
public interface JobDivided<T extends PowerJob> {

	/**
	 * @return first job instance
	 */
	@Nullable
	T getFirstInstance();

	/**
	 * @return second job instance
	 */
	T getSecondInstance();
}
