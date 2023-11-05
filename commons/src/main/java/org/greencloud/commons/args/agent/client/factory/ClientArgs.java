package org.greencloud.commons.args.agent.client.factory;

import static java.time.Instant.now;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.enums.agent.ClientTimeTypeEnum;
import org.immutables.value.Value.Immutable;

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
				deadline.plus(48, ChronoUnit.HOURS) :
				deadline;
		final String dateFormat = "dd/MM/yyyy HH:mm";
		return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC")).format(date);
	}

	/**
	 * Method creates from current time by adding given number of seconds
	 *
	 * @param seconds number of seconds
	 * @return formatted time string
	 */
	default String formatClientTime(final long seconds) {
		final Instant date = now().plusSeconds(INITIAL_SEC + seconds);
		final String dateFormat = "dd/MM/yyyy HH:mm";
		return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC")).format(date);
	}

}
