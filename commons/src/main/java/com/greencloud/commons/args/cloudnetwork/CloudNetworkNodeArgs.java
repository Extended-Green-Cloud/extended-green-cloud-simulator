package com.greencloud.commons.args.cloudnetwork;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.greencloud.commons.args.AgentArgs;

@JsonSerialize(as = ImmutableCloudNetworkNodeArgs.class)
@JsonDeserialize(as = ImmutableCloudNetworkNodeArgs.class)
@Value.Immutable
public interface CloudNetworkNodeArgs extends AgentArgs {

	Double getMaximumCapacity();

	List<String> getServerAgents();
}
