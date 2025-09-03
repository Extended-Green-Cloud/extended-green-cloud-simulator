package org.greencloud.commons.args.agent;

import java.util.List;
import java.util.function.Predicate;

import org.jrba.agentmodel.domain.props.AgentProps;
import org.jrba.agentmodel.types.AgentType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EGCSAgentType implements AgentType {

	SERVER("SERVER"),
	GREEN_ENERGY("GREEN_ENERGY"),
	REGIONAL_MANAGER("REGIONAL_MANAGER"),
	CLIENT("CLIENT"),
	MONITORING("MONITORING"),
	CENTRAL_MANAGER("CENTRAL_MANAGER"),
	MANAGING("MANAGING");

	final String name;

	/**
	 * Function evaluating if the agent belongs to cloud infrastructure
	 */
	public static final Predicate<AgentProps> isCloudAgent = props ->
			!List.of(CLIENT.name(), MANAGING.name()).contains(props.getAgentType());

	/**
	 * Function evaluating if the agent represents cloud client
	 */
	public static final Predicate<AgentProps> isClient = props -> props.getAgentType().equals(CLIENT.name());

}
