package com.greencloud.application.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.AbstractAgent;
import com.gui.agents.AbstractAgentNode;
import com.gui.controller.GuiController;

public class GuiConnectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(GuiConnectionProvider.class);

	/**
	 * Method connects agent with GUI
	 *
	 * @param abstractAgent agent connected to GUI
	 */
	public static void connectToGui(AbstractAgent abstractAgent) {
		connectAgentObject(abstractAgent, 0, abstractAgent.getO2AObject());
		connectAgentObject(abstractAgent, 1, abstractAgent.getO2AObject());
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
		}
		if (objectCounter == 1) {
			logger.info("[{}] Agent connected with the controller", abstractAgent.getName());
		}
	}
}
