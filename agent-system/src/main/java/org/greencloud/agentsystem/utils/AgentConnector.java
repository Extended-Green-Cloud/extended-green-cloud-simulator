package org.greencloud.agentsystem.utils;

import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.slf4j.Logger;

import com.gui.agents.AbstractNode;
import com.gui.controller.GuiController;

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
	 * @param objectCounter connected objects counter
	 * @param currentObject object to be connected with agent
	 */
	public static void connectAgentObject(AbstractAgent abstractAgent, Integer objectCounter, Object currentObject) {
		if (currentObject instanceof GuiController guiController) {
			abstractAgent.setGuiController(guiController);
		} else if (currentObject instanceof AbstractNode node) {
			abstractAgent.setAgentNode(node);
		} else if (currentObject instanceof RulesController rulesController) {
			abstractAgent.setRulesController(rulesController);
			logger.info("[{}] Agent connected with the rules controller", abstractAgent.getName());
		}
		if (objectCounter == 1) {
			logger.info("[{}] Agent connected with the GUI controller", abstractAgent.getName());
		}
	}

}
