package com.greencloud.connector.factory;

import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.utils.factory.AgentControllerFactory;

import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.wrapper.AgentController;

/**
 * Factory used to create and run agent controllers
 */
public interface EGCSControllerFactory extends AgentControllerFactory {

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs agent arguments
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs);

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs agent arguments
	 * @param agentNode GUI agent node
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs, EGCSNode<?, ?> agentNode);

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs agent arguments
	 * @param scenario  which has to be passed to managing agent
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs, ScenarioStructureArgs scenario);

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs     agent arguments
	 * @param scenario      which has to be passed to managing agent
	 * @param isInformer    flag indicating if the created controller should send starting information to managing agent
	 * @param managingAgent AID of managing agent with which the given agent should communicate
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs,
			ScenarioStructureArgs scenario,
			boolean isInformer,
			AID managingAgent);

	/**
	 * Method returns database instance
	 *
	 * @return TimeScale Database
	 */
	TimescaleDatabase getDatabase();
}
