package org.greencloud.commons.args.event;

import static org.greencloud.commons.enums.event.EventTypeEnum.NEW_CLIENT_EVENT;

import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.exception.InvalidScenarioEventStructure;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
	 * @return job sent by the client
	 */
	JobArgs getJob();

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
		getJob().check();
	}
}
