package com.greencloud.application.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.AbstractAgent;
import com.gui.agents.AbstractAgentNode;
import com.gui.controller.GuiController;

public class GuiConnectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(GuiConnectionProvider.class);

	public static void connectToGui(AbstractAgent abstractAgent) {
		connect(abstractAgent, 0);
		connect(abstractAgent, 1);
	}

	private static void connect(AbstractAgent abstractAgent, Integer objectCounter) {
		final Object object = abstractAgent.getO2AObject();

		if (object instanceof GuiController guiController) {
			abstractAgent.setGuiController(guiController);
		} else if (object instanceof AbstractAgentNode node) {
			abstractAgent.setAgentNode(node);
		}
		if (objectCounter == 1) {
			logger.info("[{}] Agent connected with the controller", abstractAgent.getName());
		}
	}
}
