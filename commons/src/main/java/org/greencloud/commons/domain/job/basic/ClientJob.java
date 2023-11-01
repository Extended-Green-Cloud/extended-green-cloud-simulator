package org.greencloud.commons.domain.job.basic;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the data describing the client's job
 */
@JsonSerialize(as = ImmutableClientJob.class)
@JsonDeserialize(as = ImmutableClientJob.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
	 * @return optional server selection preference specified in Expression Language
	 */
	@Nullable
	String getSelectionPreference();

}
