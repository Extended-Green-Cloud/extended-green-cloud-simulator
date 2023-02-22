package com.greencloud.application.agents;

import static com.greencloud.commons.agent.AgentType.CLIENT;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.gui.agents.AbstractAgentNode;
import com.gui.controller.GuiController;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * Abstract class representing agent which has the connection with GUI controller
 */
public abstract class AbstractAgent extends Agent {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);

	protected AgentType agentType;
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
		logger.info("I'm finished. Bye!");
		super.takeDown();
	}

	@Override
	public void clean(boolean ok) {
		if(!ok && Objects.nonNull(getGuiController()) && !agentType.equals(CLIENT)) {
			getGuiController().removeAgentNodeFromGraph(getAgentNode());
		}
		super.clean(ok);
	}

	public AgentType getAgentType() {
		return agentType;
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

	public boolean executeAction(AdaptationAction adaptationAction, AdaptationActionParameters actionParameters) {
		// this method must be overwritten in agent types that will be a target to adaptation
		throw new UnsupportedOperationException();
	}

	public void executeAction(AdaptationAction adaptationAction, AdaptationActionParameters actionParameters,
			ACLMessage adaptationMessage) {
		// this method can be overwritten in agent types that will be a target to adaptation
		throw new UnsupportedOperationException();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		guiController.addAgentNodeToGraph(agentNode);
		agentNode.setDatabaseClient(new TimescaleDatabase());
	}
}
