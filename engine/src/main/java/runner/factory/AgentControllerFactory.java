package runner.factory;

import com.greencloud.commons.args.AgentArgs;
import com.gui.agents.AbstractAgentNode;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import runner.domain.ScenarioArgs;

/**
 * Factory used to create agent controllers
 */
public interface AgentControllerFactory {

	/**
	 * Method creates the agent controllers
	 *
	 * @param agentArgs agent arguments
	 * @return AgentController that can be started
	 */
	AgentController createAgentController(AgentArgs agentArgs) throws StaleProxyException;

	/**
	 * Method creates the graph node based on the scenario arguments
	 *
	 * @param agentArgs    current agent arguments
	 * @param scenarioArgs scenario arguments
	 */
	AbstractAgentNode createAgentNode(AgentArgs agentArgs, ScenarioArgs scenarioArgs);

}
