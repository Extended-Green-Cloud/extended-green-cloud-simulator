package org.greencloud.commons.domain.job.basic;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.ImmutableConfig;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.errorprone.annotations.Var;

/**
 * Object storing the data describing the client's job
 */
@JsonSerialize(as = ImmutableClientJob.class)
@JsonDeserialize(as = ImmutableClientJob.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Value.Style(underrideHashCode = "hash", underrideEquals = "equalTo")
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

	/**
	 * @return name of the job type
	 */
	String getJobType();

	/**
	 * @return optional server selection preference specified in Expression Language
	 */
	@Nullable
	String getSelectionPreference();

	@Override
	default int hash() {
		@Var int h = 5381;
		h += (h << 5) + getJobInstanceId().hashCode();
		return h;
	}

	default boolean equalTo(ImmutableClientJob another) {
		if (this == another)
			return true;
		return another != null && getJobInstanceId().equals(another.getJobInstanceId());
	}
}
