package org.greencloud.commons.args.agent.server.node;

import java.util.List;

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
	 * @return number of CPU cores of given server
	 */
	Long getCpu();

	/**
	 * @return memory of given server
	 */
	Long getMemory();

	/**
	 * @return storage of given server
	 */
	Long getStorage();

	/**
	 * @return price per power unit of given server
	 */
	Double getPrice();
}
