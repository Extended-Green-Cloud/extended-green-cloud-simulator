package org.greencloud.commons.domain.resources;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Satisfaction of matching executors and job
 */
@JsonSerialize(as = ImmutableResourceSatisfactionMatch.class)
@JsonDeserialize(as = ImmutableResourceSatisfactionMatch.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface ResourceSatisfactionMatch {

	/**
	 * @return satisfaction of executor
	 */
	@Nullable
	Double getExecutorSatisfaction();

	/**
	 * @return satisfaction of job
	 */
	@Nullable
	Double getJobSatisfaction();
}
