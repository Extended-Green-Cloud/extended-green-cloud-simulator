package domain.job;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the data describing the client's job
 */
@JsonSerialize(as = ImmutableClientJob.class)
@JsonDeserialize(as = ImmutableClientJob.class)
@Immutable
public interface ClientJob extends PowerJob {

	/**
	 * @return unique client identifier (client global name)
	 */
	String getClientIdentifier();

}
