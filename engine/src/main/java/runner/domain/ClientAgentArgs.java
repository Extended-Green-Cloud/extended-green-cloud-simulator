package runner.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value.Immutable;

/**
 * Arguments of Client Agent
 */
@JsonSerialize(as = ImmutableClientAgentArgs.class)
@JsonDeserialize(as = ImmutableClientAgentArgs.class)
@Immutable
public interface ClientAgentArgs extends AgentArgs {

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return number of hours after which the job execution should start
	 */
	String getStart();

	/**
	 * @return number of hours after which the job execution should finish
	 */
	String getEnd();

	/**
	 * @return power required for the job
	 */
	String getPower();

}
