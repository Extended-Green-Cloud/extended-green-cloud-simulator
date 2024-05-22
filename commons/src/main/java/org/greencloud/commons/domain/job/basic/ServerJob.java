package org.greencloud.commons.domain.job.basic;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.Var;

import jade.core.AID;

/**
 * Object storing the data describing the job which execution is associated with the given server
 */
@JsonSerialize(as = ImmutableServerJob.class)
@JsonDeserialize(as = ImmutableServerJob.class)
@Value.Style(underrideHashCode = "hash", underrideEquals = "equalTo")
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

	@Override
	default int hash() {
		@Var int h = 5381;
		h += (h << 5) + getJobInstanceId().hashCode();
		return h;
	}

	default boolean equalTo(ImmutableServerJob another) {
		if (this == another)
			return true;
		return another != null && getJobInstanceId().equals(another.getJobInstanceId());
	}
}
