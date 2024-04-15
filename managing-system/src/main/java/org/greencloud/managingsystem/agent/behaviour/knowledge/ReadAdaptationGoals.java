package org.greencloud.managingsystem.agent.behaviour.knowledge;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.monitoring.MonitorSystemState;

import com.greencloud.connector.factory.EGCSControllerFactoryImpl;

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
		managingAgent.addBehaviour(new MonitorSystemState(managingAgent));
		managingAgent.execute().setFactory(new EGCSControllerFactoryImpl(managingAgent.getContainerController(),
				managingAgent.getAgentNode().getDatabaseClient(), managingAgent.getGuiController()));

		managingAgent.getAgentNode().getDatabaseClient().readAdaptationActions()
				.forEach(action -> managingAgent.getAgentNode().updateAdaptationAction(action));
	}
}
