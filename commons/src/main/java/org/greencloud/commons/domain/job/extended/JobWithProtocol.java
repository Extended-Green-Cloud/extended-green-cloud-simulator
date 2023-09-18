package org.greencloud.commons.domain.job.extended;

import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object created for messaging purposes so that the job can be sent together with the
 * message protocol that the sender expects to receive in the reply
 */
@JsonSerialize(as = ImmutableJobWithProtocol.class)
@JsonDeserialize(as = ImmutableJobWithProtocol.class)
@Value.Immutable
@ImmutableConfig
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
