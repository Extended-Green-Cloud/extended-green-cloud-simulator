package org.greencloud.commons.args.agent.cloudnetwork.node;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to construct GUI node of Cloud Network Agent
 */
@JsonSerialize(as = ImmutableCloudNetworkNodeArgs.class)
@JsonDeserialize(as = ImmutableCloudNetworkNodeArgs.class)
@Value.Immutable
public interface CloudNetworkNodeArgs extends AgentArgs {

	List<String> getServerAgents();
	Map<String, Resource> getOwnedResources();

	Double getMaxServerCpu();
}
