package com.greencloud.commons.domain.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.domain.ImmutableConfig;

import jade.core.AID;

/**
 * Object storing the data describing the job which execution is associated with the given server
 */
@JsonSerialize(as = ImmutableServerJob.class)
@JsonDeserialize(as = ImmutableServerJob.class)
@Value.Immutable
@ImmutableConfig
public interface ServerJob extends PowerJob {

	/**
	 * @return identifier of the server which sent the given job
	 */
	AID getServer();

	/**
	 * @return power required to execute a given job (value per single time unit)
	 */
	Double getEstimatedEnergy();
}
