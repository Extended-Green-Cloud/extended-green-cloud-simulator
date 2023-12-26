package org.greencloud.commons.args.adaptation.system;

import java.util.List;

import org.greencloud.commons.args.agent.AgentArgs;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;

import jade.core.AID;
import jade.core.Location;

/**
 * Content of the message sent when the adaptation plan which adds additional server is
 * executed
 */
public class AddServerActionParameters implements SystemAdaptationActionParameters {

	/**
	 * Arguments of the server agent that should be added to the system within the adaptation action.
	 */
	private final ServerArgs serverAgentArgs;
	/**
	 * Arguments of the green energy source agent that should be added to the system within the adaptation action.
	 */
	private final GreenEnergyArgs greenEnergyAgentArgs;
	/**
	 * Arguments of the green energy source agent that should be added to the system within the adaptation action.
	 */
	private final MonitoringArgs monitoringAgentArgs;
	/**
	 * In case of multi container scenario location to which the newly created agents should be moved.
	 */
	private final Location agentsTargetLocation;
	/**
	 * In case of multi-platform scenario location to which the newly created agents should be moved.
	 */
	private final AID agentsTargetAMS;

	public AddServerActionParameters(ServerArgs serverAgentArgs, GreenEnergyArgs greenEnergyAgentArgs,
			MonitoringArgs monitoringAgentArgs, Location agentsTargetLocation, AID agentsTargetAMS) {
		this.serverAgentArgs = serverAgentArgs;
		this.greenEnergyAgentArgs = greenEnergyAgentArgs;
		this.monitoringAgentArgs = monitoringAgentArgs;
		this.agentsTargetLocation = agentsTargetLocation;
		this.agentsTargetAMS = agentsTargetAMS;
	}

	@Override
	public List<AgentArgs> getAgentsArguments() {
		return List.of(monitoringAgentArgs, greenEnergyAgentArgs, serverAgentArgs);
	}

	@Override
	public Location getAgentsTargetLocation() {
		return agentsTargetLocation;
	}

	@Override
	public AID getAgentsTargetAMS() {
		return agentsTargetAMS;
	}
}
