package com.greencloud.application.domain.job;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



/**
 * Object created for messaging purposes so that the job can be sent together with the
 * message protocol that the sender expects to receive in the reply
 */
@JsonSerialize(as = ImmutableJobWithProtocol.class)
@JsonDeserialize(as = ImmutableJobWithProtocol.class)
@Value.Immutable
public interface JobWithProtocol {

	/**
	 * @return unique job instance id
	 */
	JobInstanceIdentifier getJobInstanceIdentifier();

	/**
	 * @return reply protocol
	 */
	String getReplyProtocol();
}
