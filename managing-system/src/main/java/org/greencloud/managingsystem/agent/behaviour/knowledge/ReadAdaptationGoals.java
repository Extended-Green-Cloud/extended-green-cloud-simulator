package org.greencloud.managingsystem.agent.behaviour.knowledge;

import org.greencloud.managingsystem.agent.ManagingAgent;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour is responsible for reading current adaptation goals from the system knowledge
 */
public class ReadAdaptationGoals extends OneShotBehaviour {

	private ManagingAgent managingAgent;

	/**
	 * Method casts the abstract agent to the agent of type Managing Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.managingAgent = (ManagingAgent) myAgent;
	}

	/**
	 * Method reads the adaptation goals' properties from the database
	 */
	@Override
	public void action() {
		managingAgent.monitor().readSystemAdaptationGoals();
	}
}
