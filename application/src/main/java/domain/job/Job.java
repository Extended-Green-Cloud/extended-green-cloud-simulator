package domain.job;

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
public interface Job extends JobId{

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return unique client identifier (client global name)
	 */
	String getClientIdentifier();

	/**
	 * @return time when the job execution should start
	 */
	Instant getStartTime();

	/**
	 * @return time when the job execution should finish
	 */
	Instant getEndTime();

	/**
	 * @return power necessary to execute the job
	 */
	int getPower();
}
