package com.greencloud.commons.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

/**
 * Object storing the data describing the job send by the given server
 */
@JsonSerialize(as = ImmutableServerJob.class)
@JsonDeserialize(as = ImmutableServerJob.class)
@Value.Immutable
public interface ServerJob extends PowerJob {

	/**
	 * @return identifier of the server which sent the given job
	 */
	AID getServer();
}
