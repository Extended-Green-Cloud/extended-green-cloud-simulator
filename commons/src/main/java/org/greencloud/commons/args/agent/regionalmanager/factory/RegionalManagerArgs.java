package org.greencloud.commons.args.agent.regionalmanager.factory;

import org.jrba.agentmodel.domain.args.AgentArgs;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Regional Manager Agent
 */
@JsonSerialize(as = ImmutableRegionalManagerArgs.class)
@JsonDeserialize(as = ImmutableRegionalManagerArgs.class)
@Immutable
public interface RegionalManagerArgs extends AgentArgs {

	/**
	 * @return preferred maximum scheduled job queue size
	 */
	Integer getMaximumQueueSize();

	/**
	 * @return a size of a batch being the number of jobs drawn from the queue
	 */
	Integer getPollingBatchSize();

	/**
	 * @return location's latitude
	 */
	@Nullable
	String getLatitude();

	/**
	 * @return location's longitude
	 */
	@Nullable
	String getLongitude();

	@Nullable
	String getLocationId();
}
