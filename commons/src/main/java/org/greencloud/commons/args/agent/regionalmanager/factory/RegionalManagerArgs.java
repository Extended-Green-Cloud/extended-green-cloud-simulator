package org.greencloud.commons.args.agent.regionalmanager.factory;

import org.greencloud.commons.args.agent.AgentArgs;
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
