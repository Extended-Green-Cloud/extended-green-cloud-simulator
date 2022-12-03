package com.greencloud.commons.managingsystem.planner;

import java.util.List;

import com.greencloud.commons.args.agent.AgentArgs;

import jade.core.Location;

public interface SystemAdaptationActionParameters extends AdaptationActionParameters {

	List<AgentArgs> getAgentsArguments();

	Location getAgentsTargetLocation();
}
