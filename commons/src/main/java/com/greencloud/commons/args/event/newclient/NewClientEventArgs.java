package com.greencloud.commons.args.event.newclient;

import static com.greencloud.commons.args.event.EventTypeEnum.NEW_CLIENT_EVENT;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.event.EventArgs;
import com.greencloud.commons.exception.InvalidScenarioEventStructure;

/**
 * Interface containing properties of scenario event that generates new client in Cloud Network
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNewClientEventArgs.class)
@JsonDeserialize(as = ImmutableNewClientEventArgs.class)
@JsonTypeName("NEW_CLIENT_EVENT")
public interface NewClientEventArgs extends EventArgs {

	/**
	 * @return name of the new client agent
	 */
	String getName();

	/**
	 * @return unique job identifier
	 */
	Integer getJobId();

	/**
	 * @return number of seconds after which the job execution should start (from Client creation)
	 */
	Integer getStart();

	/**
	 * @return number of seconds after which the job execution should finish (from Client creation)
	 */
	Integer getEnd();

	/**
	 * @return number of seconds after which the job will reach execution deadline (from Client creation)
	 */
	Integer getDeadline();

	/**
	 * @return power required for the job
	 */
	Integer getPower();

	/**
	 * Method verifies the correctness of new client event structure
	 */
	@Override
	@Value.Check
	default void check() {
		EventArgs.super.check();

		if (!getType().equals(NEW_CLIENT_EVENT)) {
			throw new InvalidScenarioEventStructure("Invalid event type. Acceptable event type is: NEW_CLIENT_EVENT");
		}
		if (getJobId() < 1) {
			throw new InvalidScenarioEventStructure(
					String.format("Given job id: %d is invalid. The job id must be at least equal to 1", getJobId()));
		}
		if (getStart() < 1) {
			throw new InvalidScenarioEventStructure(
					String.format("Given start time: %d is invalid. The start time must be at least equal to 1",
							getStart()));
		}
		if (getEnd() <= getStart()) {
			throw new InvalidScenarioEventStructure(
					String.format("Given end time: %d is invalid. The end time must be greater than start time (%d)",
							getEnd(), getStart()));
		}
		if (getDeadline() < getEnd()) {
			throw new InvalidScenarioEventStructure(
					String.format(
							"Given deadline time: %d is invalid. The deadline time must be greater or equal to the end time (%d)",
							getEnd(), getStart()));
		}
		if (getPower() <= 0) {
			throw new InvalidScenarioEventStructure(
					String.format("Given power: %d is invalid. The power must be a positive number", getPower()));
		}
	}
}
