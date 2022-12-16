package org.greencloud.managingsystem.service.executor.jade;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.greencloud.commons.args.agent.AgentArgs;
import com.gui.controller.GuiController;

import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class AgentRunner {

	private static final Long GRAPH_INITIALIZATION_PAUSE = 7L;
	private static final Integer RUN_AGENT_PAUSE = 100;

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

	public void runAgents(List<AgentController> controllers) {
		var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.schedule(() -> controllers.forEach(controller -> runAgent(controller, RUN_AGENT_PAUSE)),
				GRAPH_INITIALIZATION_PAUSE, SECONDS);
		shutdownAndAwaitTermination(scheduledExecutor);
	}

	private void runAgent(AgentController controller, long pause) {
		try {
			controller.start();
			controller.activate();
			TimeUnit.MILLISECONDS.sleep(pause);
		} catch (StaleProxyException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new JadeControllerException("Failed to run agent controller", e);
		}
	}

	private void shutdownAndAwaitTermination(java.util.concurrent.ExecutorService executorService) {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException ie) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
