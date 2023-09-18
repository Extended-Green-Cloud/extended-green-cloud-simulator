package org.greencloud.commons.args.agent.cloudnetwork.factory;

import org.greencloud.commons.args.agent.AgentArgs;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Cloud Network Agent
 */
@JsonSerialize(as = ImmutableCloudNetworkArgs.class)
@JsonDeserialize(as = ImmutableCloudNetworkArgs.class)
@Immutable
public interface CloudNetworkArgs extends AgentArgs {

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
