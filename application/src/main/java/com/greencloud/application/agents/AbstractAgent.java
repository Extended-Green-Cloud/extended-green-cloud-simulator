package com.greencloud.application.agents;

import java.util.Objects;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.greencloud.application.behaviours.ReportHealthCheck;
import com.gui.agents.AbstractAgentNode;
import com.gui.controller.GuiController;

import jade.core.Agent;

/**
 * Abstract class representing agent which has the connection with GUI controller
 */
public abstract class AbstractAgent extends Agent {

	private GuiController guiController;
	private AbstractAgentNode agentNode;

	protected AbstractAgent() {
	}

	@Override
	protected void setup() {
		super.setup();
		setEnabledO2ACommunication(true, 2);
	}

	@Override
	protected void takeDown() {
		if (Objects.nonNull(getGuiController())) {
			getGuiController().removeAgentNodeFromGraph(getAgentNode());
		}
		super.takeDown();
	}

	public AbstractAgentNode getAgentNode() {
		return agentNode;
	}

	public void setAgentNode(AbstractAgentNode agentNode) {
		this.agentNode = agentNode;
	}

	public GuiController getGuiController() {
		return guiController;
	}

	public void setGuiController(GuiController guiController) {
		this.guiController = guiController;
	}

	public void writeMonitoringData(DataType dataType, MonitoringData monitoringData) {
		agentNode.getDatabaseClient().writeMonitoringData(this.getAID().getName(), dataType, monitoringData);
	}
}
