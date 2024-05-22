package org.greencloud.commons.domain.resources;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Match between job and resources
 */
@JsonSerialize(as = ImmutableResourceMatch.class)
@JsonDeserialize(as = ImmutableResourceMatch.class)
@Value.Immutable
public interface ResourceMatch {

	/**
	 * @return identifier of executor
	 */
	String getExecutorId();

	/**
	 * @return identifier job
	 */
	String getJobId();
}
