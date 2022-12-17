package com.greencloud.commons.managingsystem.planner;

import java.util.List;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.monitoring.MonitoringAgentArgs;

import jade.core.Location;

public class AddGreenSourceActionParameters implements SystemAdaptationActionParameters {

	/**
	 * Arguments of the green energy source agent that should be added to the system within the adaptation action.
	 */
	private final GreenEnergyAgentArgs greenEnergyAgentArgs;
	/**
	 * Arguments of the green energy source agent that should be added to the system within the adaptation action.
	 */
	private final MonitoringAgentArgs monitoringAgentArgs;
	/**
	 * In case of multi container scenario location to which the newly created agents should be moved.
	 */
	private final Location agentsTargetLocation;

	public AddGreenSourceActionParameters(GreenEnergyAgentArgs greenEnergyAgentArgs,
			MonitoringAgentArgs monitoringAgentArgs, Location agentsTargetLocation) {
		this.greenEnergyAgentArgs = greenEnergyAgentArgs;
		this.monitoringAgentArgs = monitoringAgentArgs;
		this.agentsTargetLocation = agentsTargetLocation;
	}

	@Override
	public List<AgentArgs> getAgentsArguments() {
		return List.of(monitoringAgentArgs, greenEnergyAgentArgs);
	}

	@Override
	public Location getAgentsTargetLocation() {
		return agentsTargetLocation;
	}
}
