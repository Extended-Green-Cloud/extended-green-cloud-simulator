package org.greencloud.commons.args.adaptation.system;

import java.util.List;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.args.agent.AgentArgs;

import jade.core.AID;
import jade.core.Location;

public interface SystemAdaptationActionParameters extends AdaptationActionParameters {

	List<AgentArgs> getAgentsArguments();

	Location getAgentsTargetLocation();

	AID getAgentsTargetAMS();
}
