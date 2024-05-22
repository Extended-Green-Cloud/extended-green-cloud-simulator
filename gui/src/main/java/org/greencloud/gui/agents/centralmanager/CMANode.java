package org.greencloud.gui.agents.centralmanager;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.util.LinkedList;
import java.util.Optional;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.args.agent.centralmanager.node.CentralManagerNodeArgs;
import org.greencloud.commons.domain.job.instance.ImmutableJobInstanceCMA;
import org.greencloud.commons.domain.job.instance.JobInstanceCMA;
import org.greencloud.gui.agents.egcs.EGCSNode;
import org.greencloud.gui.messages.ImmutableUpdateJobQueueMessage;
import org.greencloud.gui.messages.ImmutableUpdateSingleValueMessage;
import org.jrba.environment.domain.ExternalEvent;

/**
 * Agent node class representing the Central Manager Agent.
 */
public class CMANode extends EGCSNode<CentralManagerNodeArgs, CentralManagerAgentProps> {

	/**
	 * CMA node constructor
	 *
	 * @param args arguments provided for CMA creation
	 */
	public CMANode(CentralManagerNodeArgs args) {
		super(args, CENTRAL_MANAGER);
	}

	/**
	 * Method updates CMA's GUI by setting new job queue
	 *
	 * @param agentProps current properties of CMA
	 */
	public void updateScheduledJobQueue(final CentralManagerAgentProps agentProps) {
		var queueCopy = new LinkedList<>(agentProps.getJobsToBeExecuted());
		var mappedQueue = new LinkedList<JobInstanceCMA>();

		queueCopy.iterator().forEachRemaining(el -> mappedQueue.add(ImmutableJobInstanceCMA.builder()
				.jobId(el.getJobId())
				.clientName(el.getClientIdentifier())
				.build()));

		getAgentsWebSocket().send(ImmutableUpdateJobQueueMessage.builder().data(mappedQueue).build());
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

	public Optional<ExternalEvent> getEvent() {
		return ofNullable(eventsQueue.poll());
	}

	@Override
	public void updateGUI(final CentralManagerAgentProps props) {
		// CMA does not report any data
	}

	@Override
	public void saveMonitoringData(final CentralManagerAgentProps props) {
		// CMA does not report any data
	}
}
