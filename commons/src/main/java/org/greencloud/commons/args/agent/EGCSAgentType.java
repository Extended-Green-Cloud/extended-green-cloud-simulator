package org.greencloud.commons.args.agent;

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
	SCHEDULER("SCHEDULER"),
	MANAGING("MANAGING");

	final String name;
}
