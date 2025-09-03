package org.greencloud.managingsystem.behaviours.knowledge;

import org.greencloud.managingsystem.agents.ManagingAgent;
import org.greencloud.managingsystem.behaviours.monitoring.MonitorSystemState;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour is responsible for reading current adaptation goals from the system knowledge.
 */
public class ReadAdaptationGoals extends OneShotBehaviour {

	private final ManagingAgent managingAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param managingAgent managing agent executing the behaviour
	 */
	public ReadAdaptationGoals(final ManagingAgent managingAgent) {
		this.managingAgent = managingAgent;
	}

	/**
	 * Method reads the adaptation goal properties from the database
	 */
	@Override
	public void action() {
		managingAgent.monitor().readSystemAdaptationGoals();
		managingAgent.addBehaviour(new MonitorSystemState(managingAgent));
		managingAgent.getAgentNode().getDatabaseClient().readAdaptationActions()
				.forEach(action -> managingAgent.getAgentNode().updateAdaptationAction(action));
	}
}
