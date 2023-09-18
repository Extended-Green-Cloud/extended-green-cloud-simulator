package org.greencloud.commons.domain.job.basic;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data describing the client's job
 */
@JsonSerialize(as = ImmutableClientJob.class)
@JsonDeserialize(as = ImmutableClientJob.class)
@Value.Immutable
@ImmutableConfig
public interface ClientJob extends PowerJob {

	/**
	 * @return unique client identifier (client global name)
	 */
	String getClientIdentifier();

	/**
	 * @return unique client identifier (client global name)
	 */
	String getClientAddress();

}
