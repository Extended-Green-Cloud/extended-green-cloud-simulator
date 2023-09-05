package com.greencloud.commons.domain.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing data allowing to identify given scheduled job
 */
@JsonSerialize(as = ImmutableScheduledJobIdentity.class)
@JsonDeserialize(as = ImmutableScheduledJobIdentity.class)
@Value.Immutable
public interface ScheduledJobIdentity {

	/**
	 * @return job identifier
	 */
	String getJobId();

	/**
	 * @return name of the client
	 */
	String getClientName();
}
