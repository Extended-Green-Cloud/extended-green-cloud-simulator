package org.greencloud.commons.domain.resources;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Preferences of executors and job
 */
@JsonSerialize(as = ImmutableResourcePreferenceMatch.class)
@JsonDeserialize(as = ImmutableResourcePreferenceMatch.class)
@Value.Immutable
public interface ResourcePreferenceMatch {

	/**
	 * @return preference of executor
	 */
	Double getExecutorPreference();

	/**
	 * @return preference of job
	 */
	Double getJobPreference();
}
