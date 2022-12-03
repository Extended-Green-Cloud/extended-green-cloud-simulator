package com.greencloud.commons.args.agent.client;

import static com.greencloud.commons.args.agent.client.ClientTimeType.SIMULATION;
import static com.greencloud.commons.time.TimeConstants.SECONDS_PER_HOUR;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.agent.AgentArgs;

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
	 * @return type of time used to specify time bounds
	 */
	ClientTimeType getTimeType();

	/**
	 * @return number of hours/simulation seconds after which the job execution should start (from Client creation)
	 */
	String getStart();

	/**
	 * @return number of hours/simulation seconds after which the job execution should finish (from Client creation)
	 */
	String getEnd();

	/**
	 * @return number of hours/simulation seconds after which the job execution has to end (from Client creation)
	 */
	String getDeadline();

	/**
	 * @return power required for the job
	 */
	String getPower();

	/**
	 * Method converts the number of hours/seconds to formatted time string
	 *
	 * @param value number of hours/seconds
	 * @return formatted time string
	 */
	default String formatClientTime(final String value) {
		return getTimeType().equals(SIMULATION)
				? formatToDateFromSeconds(value)
				: formatToDateFromHours(value);
	}

	private String formatToDateFromHours(final String value) {
		final Instant date = Instant.now().plus(Long.parseLong(value), ChronoUnit.HOURS);
		final String dateFormat = "dd/MM/yyyy HH:mm";
		return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC")).format(date);
	}

	private String formatToDateFromSeconds(final String value) {
		final double hours = Double.parseDouble(value) * ((double) 1 / SECONDS_PER_HOUR);
		final long milliseconds = (long) (3600 * hours * 1000);

		final Instant date = Instant.now().plus(milliseconds, ChronoUnit.MILLIS);
		final String dateFormat = "dd/MM/yyyy HH:mm";
		return DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId.of("UTC")).format(date);
	}

}
