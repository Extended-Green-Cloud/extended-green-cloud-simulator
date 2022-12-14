package runner.factory;

import com.greencloud.commons.args.agent.AgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.AbstractAgentNode;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Factory used to create agent controllers
 */
public interface AgentControllerFactory {

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs agent arguments
	 * @param scenario  which has to be passed to managing agent
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs, ScenarioStructureArgs scenario)
			throws StaleProxyException;

	/**
	 * Method creates the graph node based on the scenario arguments
	 *
	 * @param agentArgs    current agent arguments
	 * @param scenarioArgs scenario arguments
	 */
	AbstractAgentNode createAgentNode(AgentArgs agentArgs, ScenarioStructureArgs scenarioArgs);

}
