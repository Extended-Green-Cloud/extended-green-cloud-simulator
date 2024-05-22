package org.greencloud.commons.domain.job.instance;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing data allowing to identify given scheduled job
 */
@JsonSerialize(as = ImmutableJobInstanceCMA.class)
@JsonDeserialize(as = ImmutableJobInstanceCMA.class)
@Value.Immutable
public interface JobInstanceCMA {

	/**
	 * @return job identifier
	 */
	String getJobId();

	/**
	 * @return name of the client
	 */
	String getClientName();
}
