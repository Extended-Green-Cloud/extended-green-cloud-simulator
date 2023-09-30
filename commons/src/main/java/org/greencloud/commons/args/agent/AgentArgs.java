package org.greencloud.commons.args.agent;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Common arguments of agents
 */
@JsonSerialize(as = ImmutableAgentArgs.class)
@JsonDeserialize(as = ImmutableAgentArgs.class)
@Value.Immutable
public interface AgentArgs {

	/**
	 * @return agent name
	 */
	String getName();

	/**
	 * Method returns agent arguments as object array
	 *
	 * @return object array
	 */
	default Object[] getObjectArray() {
		return new Object[] { getName() };
	}
}
