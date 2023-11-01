package org.greencloud.agentsystem.utils;

import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.slf4j.Logger;


import org.greencloud.rulescontroller.RulesController;

/**
 * Class defines set of utilities used to connect agents with object instances
 */
@SuppressWarnings("unchecked")
public class AgentConnector {

	private static final Logger logger = getLogger(AgentConnector.class);

	/**
	 * Method connects agent with given object
	 *
	 * @param abstractAgent agent to be connected with object
	 * @param currentObject object to be connected with agent
	 */
	public static void connectAgentObject(AbstractAgent abstractAgent, Object currentObject) {
		if (currentObject instanceof EGCSNode node) {
			abstractAgent.setAgentNode(node);
			logger.info("[{}] Agent connected with the GUI controller", abstractAgent.getName());
		} else if (currentObject instanceof RulesController rulesController) {
			abstractAgent.setRulesController(rulesController);
			logger.info("[{}] Agent connected with the rules controller", abstractAgent.getName());
		}
	}

}
