package com.greencloud.application.utils;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.greencloud.application.agents.AbstractAgent;
import com.gui.agents.AbstractAgentNode;
import com.gui.controller.GuiController;

import rules.RulesController;

/**
 * Class defines set of utilities used together with GUI Controller
 */
public class GuiUtils {

	private static final Logger logger = getLogger(GuiUtils.class);

	/**
	 * Method connects agent with GUI
	 *
	 * @param abstractAgent agent connected to GUI
	 */
	public static void connectToGui(AbstractAgent abstractAgent) {
		connectAgentObject(abstractAgent, 0, abstractAgent.getO2AObject());
		connectAgentObject(abstractAgent, 1, abstractAgent.getO2AObject());
		connectAgentObject(abstractAgent, 2, abstractAgent.getO2AObject());
	}

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
		} else if (currentObject instanceof AbstractAgentNode node) {
			abstractAgent.setAgentNode(node);
		} else if (currentObject instanceof RulesController rulesController) {
			abstractAgent.setRulesController(rulesController);
			logger.info("[{}] Agent connected with the rules controller", abstractAgent.getName());
		}
		if (objectCounter == 1) {
			logger.info("[{}] Agent connected with the controller", abstractAgent.getName());
		}
	}

	/**
	 * Method updates the GUI to indicate that the job execution has finished
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceFinishedJob(final AbstractAgent agent) {
		agent.getGuiController().updateActiveJobsCountByValue(-1);
		agent.getGuiController().updateAllJobsCountByValue(-1);
	}

	/**
	 * Method updates the GUI to indicate that a new job is planned to be executed
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceBookedJob(final AbstractAgent agent) {
		agent.getGuiController().updateAllJobsCountByValue(1);
	}

	/**
	 * Method updates the GUI to indicate that new client is using Cloud Network
	 *
	 * @param agent agent updating the GUI
	 */
	public static void announceNewClient(final AbstractAgent agent) {
		agent.getGuiController().updateClientsCountByValue(1);
	}
}
