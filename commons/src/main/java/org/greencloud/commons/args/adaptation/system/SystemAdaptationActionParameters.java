package org.greencloud.commons.args.adaptation.system;

import java.util.List;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.jrba.agentmodel.domain.args.AgentArgs;

import jade.core.AID;
import jade.core.Location;

/**
 * Common interface for messages that contain adaptation plan which affects more than one agent
 */
public interface SystemAdaptationActionParameters extends AdaptationActionParameters {

	/**
	 * @return arguments of the agents that are to be created
	 */
	List<AgentArgs> getAgentsArguments();

	/**
	 * @return target location at which agent is to be created
	 */
	Location getAgentsTargetLocation();

	AID getAgentsTargetAMS();
}
