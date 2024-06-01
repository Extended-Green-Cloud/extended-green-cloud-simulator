package org.greencloud.commons.domain.job.basic;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing job with assigned server
 */
@JsonSerialize(as = ImmutableClientJobWithServer.class)
@JsonDeserialize(as = ImmutableClientJobWithServer.class)
@Value.Immutable
@ImmutableConfig
public interface ClientJobWithServer extends ClientJob {

	/**
	 * @return assigned server
	 */
	@Nullable
	String getServer();
}
