package com.gui.agents;

import java.util.concurrent.PriorityBlockingQueue;

import com.greencloud.commons.args.agent.scheduler.ImmutableSchedulerNodeArgs;
import com.greencloud.commons.args.agent.scheduler.SchedulerAgentArgs;
import com.greencloud.commons.job.ClientJob;
import com.gui.message.ImmutableRegisterAgentMessage;
import com.gui.message.ImmutableSetMaximumCapacityMessage;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateJobQueueMessage;
import com.gui.websocket.GuiWebSocketClient;

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

		this.deadlinePriorityWeight = args.getDeadlineWeight();
		this.powerPriorityWeight = args.getPowerWeight();
		this.maxQueueSize = args.getMaximumQueueSize();
	}

	@Override
	public void addToGraph(GuiWebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
		webSocketClient.send(ImmutableRegisterAgentMessage.builder()
				.agentType("SCHEDULER")
				.data(ImmutableSchedulerNodeArgs.builder()
						.name(agentName)
						.deadlinePriority(deadlinePriorityWeight)
						.powerPriority(powerPriorityWeight)
						.maxQueueSize(maxQueueSize)
						.build())
				.build());
	}

	/**
	 * Method updates scheduler's GUI by setting new job queue
	 *
	 * @param updatedJobQueue current job queue
	 */
	public void updateScheduledJobQueue(final PriorityBlockingQueue<ClientJob> updatedJobQueue) {
		webSocketClient.send(ImmutableUpdateJobQueueMessage.builder()
				.data(updatedJobQueue)
				.type("UPDATE_JOB_QUEUE")
				.build());
	}
}
