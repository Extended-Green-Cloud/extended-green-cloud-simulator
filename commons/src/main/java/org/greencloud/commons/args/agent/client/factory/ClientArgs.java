package org.greencloud.commons.args.agent.client.factory;

import static java.time.Instant.now;
import static org.greencloud.commons.utils.time.TimeConverter.convertInstantToString;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.immutables.value.Value.Immutable;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Client Agent
 */
@JsonSerialize(as = ImmutableClientArgs.class)
@JsonDeserialize(as = ImmutableClientArgs.class)
@Immutable
public interface ClientArgs extends AgentArgs {

	int INITIAL_SEC = 3;

	/**
	 * @return unique job identifier
	 */
	String getJobId();

	/**
	 * @return type of time used to specify time bounds
	 */
	ClientTimeTypeEnum getTimeType();

	/**
	 * @return job sent by the client
	 */
	JobArgs getJob();

	/**
	 * Method creates formatted deadline date
	 *
	 * @return formatted time string
	 */
	default String formatClientDeadline() {
		final Instant deadline = now().plusSeconds(INITIAL_SEC + getJob().getDeadline() + getJob().getDuration());
		final Instant date = getJob().getDeadline().equals(0L) ?
				deadline.plus(4, ChronoUnit.HOURS) :
				deadline;
		return convertInstantToString(date);
	}
}
