package org.greencloud.commons.domain.jobstep;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

@JsonSerialize(as = ImmutableJobStep.class)
@JsonDeserialize(as = ImmutableJobStep.class)
@Value.Immutable
@ImmutableConfig
public interface JobStep {

	/**
	 * @return name of the step
	 */
	String getName();

	/**
	 * @return required amount of CPU (im millicores, for entire step)
	 */
	Long getCpu();

	/**
	 * @return required memory (in Mi, for entire step)
	 */
	Long getMemory();

	/**
	 * @return step execution duration (in seconds, for entire step)
	 */
	Long getDuration();
}
