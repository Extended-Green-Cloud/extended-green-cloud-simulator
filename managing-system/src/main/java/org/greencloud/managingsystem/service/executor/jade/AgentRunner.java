package org.greencloud.managingsystem.service.executor.jade;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.greencloud.commons.args.agent.AgentArgs;
import com.gui.controller.GuiController;

import jade.wrapper.AgentController;

public class AgentRunner {

	private final ManagingAgent managingAgent;
	private final AgentControllerFactory agentControllerFactory;

	public AgentRunner(ManagingAgent managingAgent, AgentControllerFactory agentControllerFactory) {
		this.managingAgent = managingAgent;
		this.agentControllerFactory = agentControllerFactory;
	}

	public AgentController runAgentController(AgentArgs args) {
		AgentController agentController = null;
		GuiController guiController = managingAgent.getGuiController();
		try {
			agentController = agentControllerFactory.createAgentController(args);
			var agentNode = agentControllerFactory.createAgentNode(args, managingAgent.getGreenCloudStructure());
			agentNode.setDatabaseClient(managingAgent.getAgentNode().getDatabaseClient());
			guiController.addAgentNodeToGraph(agentNode);
			agentController.putO2AObject(guiController, AgentController.ASYNC);
			agentController.putO2AObject(agentNode, AgentController.ASYNC);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return agentController;
	}
}
