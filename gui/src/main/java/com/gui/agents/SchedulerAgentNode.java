package com.gui.agents;

import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the scheduler agent
 */
public class SchedulerAgentNode extends AbstractAgentNode {

	final double deadlinePriorityWeight;
	final double powerPriorityWeight;

	/**
	 * Scheduler node constructor
	 *
	 * @param args arguments provided for scheduler agent creation
	 */
	public SchedulerAgentNode(SchedulerAgentArgs args) {
		super(args.getName());

		this.deadlinePriorityWeight = args.getDeadlineWeight();
		this.powerPriorityWeight = args.getPowerWeight();
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		//TO BE IMPLEMENTED
	}
}
