package com.greencloud.application.domain.job;

import java.time.Instant;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object storing the data describing the client's job
 */
@JsonSerialize(as = ImmutableJob.class)
@JsonDeserialize(as = ImmutableJob.class)
@Immutable
public interface Job extends AbstractJob {
	/**
	 * @return unique client identifier (client global name)
	 */
	String getClientIdentifier();

}
