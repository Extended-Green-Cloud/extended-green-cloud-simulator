package com.gui.agents.scheduler;

import static org.greencloud.commons.args.agent.AgentType.SCHEDULER;
import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static com.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static java.util.Optional.ofNullable;

import java.util.LinkedList;
import java.util.Optional;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.args.agent.scheduler.node.SchedulerNodeArgs;
import org.greencloud.commons.domain.job.instance.ImmutableJobInstanceScheduler;
import org.greencloud.commons.domain.job.instance.JobInstanceScheduler;

import com.gui.agents.egcs.EGCSNode;
import com.gui.event.AbstractEvent;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateJobQueueMessage;
import com.gui.message.ImmutableUpdateSingleValueMessage;

/**
 * Agent node class representing the scheduler agent
 */
public class SchedulerNode extends EGCSNode<SchedulerNodeArgs, SchedulerAgentProps> {

	/**
	 * Scheduler node constructor
	 *
	 * @param args arguments provided for scheduler agent creation
	 */
	public SchedulerNode(SchedulerNodeArgs args) {
		super(args, SCHEDULER);
	}

	/**
	 * Method updates scheduler's GUI by setting new job queue
	 *
	 * @param agentProps current properties of scheduler agent
	 */
	public void updateScheduledJobQueue(final SchedulerAgentProps agentProps) {
		var queueCopy = new LinkedList<>(agentProps.getJobsToBeExecuted());
		var mappedQueue = new LinkedList<JobInstanceScheduler>();

		queueCopy.iterator().forEachRemaining(el -> mappedQueue.add(ImmutableJobInstanceScheduler.builder()
				.jobId(el.getJobId())
				.clientName(el.getClientIdentifier())
				.build()));

		getAgentsWebSocket().send(ImmutableUpdateJobQueueMessage.builder().data(mappedQueue).build());
	}

	/**
	 * Method updates the deadline priority in the scheduler agent
	 *
	 * @param value value being new deadline priority (eg. 0.2 as for 20%)
	 */
	public void updateDeadlinePriority(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("UPDATE_SCHEDULER_DEADLINE_PRIORITY")
				.build());
	}

	/**
	 * Method updates the CPU priority in the scheduler agent
	 *
	 * @param value value being new power priority (eg. 0.2 as for 20%)
	 */
	public void updateCPUPriority(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("UPDATE_SCHEDULER_CPU_PRIORITY")
				.build());
	}

	/**
	 * Function announce new accepted job in the network
	 */
	public void announceClientJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("UPDATE_CURRENT_PLANNED_JOBS")
				.build());
	}

	/**
	 * Function updates the number of jobs planned in the system
	 */
	public void removePlannedJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(-1)
				.type("UPDATE_CURRENT_PLANNED_JOBS")
				.build());
	}

	/**
	 * Function adds new started job
	 */
	public void addStartedInCloudJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("UPDATE_CURRENT_IN_CLOUD_ACTIVE_JOBS")
				.build());
	}

	/**
	 * Function adds new started job
	 */
	public void addFinishedInCloudJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(-1)
				.type("UPDATE_CURRENT_IN_CLOUD_ACTIVE_JOBS")
				.build());
	}

	public Optional<AbstractEvent> getEvent() {
		return ofNullable(eventsQueue.poll());
	}

	@Override
	public void updateGUI(final SchedulerAgentProps props) {
		updateCPUPriority(props.getCPUPercentage());
		updateDeadlinePriority(props.getDeadlinePercentage());
	}

	@Override
	public void saveMonitoringData(final SchedulerAgentProps props) {
		// scheduler does not report any data
	}
}
