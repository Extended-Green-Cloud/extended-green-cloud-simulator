package org.greencloud.commons.args.agent.server.node;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.args.agent.AgentArgs;

/**
 * Arguments used to construct GUI node of Server Agent
 */
@JsonDeserialize(as = ImmutableServerNodeArgs.class)
@JsonSerialize(as = ImmutableServerNodeArgs.class)
@Value.Immutable
public interface ServerNodeArgs extends AgentArgs {

	/**
	 * @return owner cloud network agent name
	 */
	String getCloudNetworkAgent();

	/**
	 * @return energy agents names
	 */
	List<String> getGreenEnergyAgents();

	/**
	 * @return maximal power of given server
	 */
	Long getMaxPower();

	/**
	 * @return idle power of given server
	 */
	Long getIdlePower();

	/**
	 * @return resources of given server
	 */
	Map<String, Resource> getResources();

	/**
	 * @return resources when they are all fully occupied
	 */
	Map<String, Resource> getEmptyResources();

	/**
	 * @return price per power unit of given server
	 */
	Double getPrice();
}
