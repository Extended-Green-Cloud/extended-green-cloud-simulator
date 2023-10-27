package org.greencloud.commons.domain.jobstep;

import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
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
	 * @return required amount of resources used to process given job
	 */
	Map<String, Resource> getRequiredResources();

	/**
	 * @return step execution duration (in seconds, for entire step)
	 */
	Long getDuration();
}
