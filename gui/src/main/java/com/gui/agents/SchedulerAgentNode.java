package com.gui.agents;

import java.util.LinkedList;

import com.greencloud.commons.args.agent.scheduler.ImmutableSchedulerNodeArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.greencloud.commons.job.ClientJob;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateJobQueueMessage;
import com.gui.websocket.GuiWebSocketClient;

/**
 * Agent node class representing the scheduler agent
 */
public class SchedulerAgentNode extends AbstractAgentNode {

	final double deadlinePriorityWeight;
	final double powerPriorityWeight;
	final int maxQueueSize;

	/**
	 * Scheduler node constructor
	 *
	 * @param args arguments provided for scheduler agent creation
	 */
	public SchedulerAgentNode(SchedulerAgentArgs args) {
		super(args.getName());

		this.deadlinePriorityWeight = (double)args.getDeadlineWeight() / (args.getDeadlineWeight() + args.getPowerWeight());
		this.powerPriorityWeight = (double)args.getPowerWeight() / (args.getPowerWeight() + args.getDeadlineWeight());

		this.maxQueueSize = args.getMaximumQueueSize();
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder().agentType("SCHEDULER")
				.data(ImmutableSchedulerNodeArgs.builder().name(agentName).deadlinePriority(deadlinePriorityWeight)
						.powerPriority(powerPriorityWeight).maxQueueSize(maxQueueSize).build()).build());
	}

	/**
	 * Method updates scheduler's GUI by setting new job queue
	 *
	 * @param updatedJobQueue current job queue
	 */
	public void updateScheduledJobQueue(final LinkedList<ClientJob> updatedJobQueue) {
		webSocketClient.send(ImmutableUpdateJobQueueMessage.builder().data(updatedJobQueue).build());
	}

	/**
	 * Method updates the deadline priority in the scheduler agent
	 *
	 * @param value value being new deadline priority (eg. 0.2 as for 20%)
	 */
	public void updateDeadlinePriority(final double value) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("UPDATE_SCHEDULER_DEADLINE_PRIORITY")
				.build());
	}

	/**
	 * Method updates the power priority in the scheduler agent
	 *
	 * @param value value being new power priority (eg. 0.2 as for 20%)
	 */
	public void updatePowerPriority(final double value) {
		webSocketClient.send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("UPDATE_SCHEDULER_POWER_PRIORITY")
				.build());
	}
}
