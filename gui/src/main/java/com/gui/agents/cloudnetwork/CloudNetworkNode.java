package com.gui.agents.cloudnetwork;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static org.greencloud.commons.args.agent.AgentType.CLOUD_NETWORK;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.utils.job.JobUtils.getJobSuccessRatio;
import static com.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static com.gui.websocket.WebSocketConnections.getCloudNetworkSocket;
import static java.util.Optional.ofNullable;

import java.util.Optional;

import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.args.agent.cloudnetwork.node.CloudNetworkNodeArgs;

import com.gui.agents.EGCSNetworkNode;
import com.gui.event.AbstractEvent;
import com.gui.message.ImmutableSetNumericValueMessage;
import com.gui.message.ImmutableUpdateSingleValueMessage;

/**
 * Agent node class representing the cloud network
 */
public class CloudNetworkNode extends EGCSNetworkNode<CloudNetworkNodeArgs, CloudNetworkAgentProps> {

	/**
	 * Cloud network node constructor
	 *
	 * @param cloudNetworkArgs node arguments
	 */
	public CloudNetworkNode(final CloudNetworkNodeArgs cloudNetworkArgs) {
		super(cloudNetworkArgs, CLOUD_NETWORK);
	}

	/**
	 * Function updates the number of clients to given value
	 *
	 * @param value value indicating the client number
	 */
	public void updateClientNumber(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}

	public Optional<AbstractEvent> getEvent() {
		return ofNullable(eventsQueue.poll());
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
	 * Function removes finished job
	 */
	public void removeActiveJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(-1)
				.type("UPDATE_CURRENT_ACTIVE_JOBS")
				.build());
	}

	/**
	 * Function adds new started job
	 */
	public void addStartedJob() {
		getCloudNetworkSocket().send(ImmutableUpdateSingleValueMessage.builder()
				.data(1)
				.type("UPDATE_CURRENT_ACTIVE_JOBS")
				.build());
	}

	/**
	 * Method updates GUI of given agent node
	 *
	 * @param agentProps current properties of an agent
	 */
	@Override
	public void updateGUI(final CloudNetworkAgentProps agentProps) {
		updateClientNumber(getScheduledJobs(agentProps));
		updateJobsCount(getJobInProgressCount(agentProps));
		updateCurrentJobSuccessRatio(getSuccessRatio(agentProps));
	}

	@Override
	public void saveMonitoringData(final CloudNetworkAgentProps props) {
		final CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(getSuccessRatio(props))
				.build();
		writeMonitoringData(CLOUD_NETWORK_MONITORING, cloudNetworkMonitoringData, props.getAgentName());
	}

	private double getSuccessRatio(final CloudNetworkAgentProps props) {
		return getJobSuccessRatio(props.getJobCounters().get(ACCEPTED).getCount(),
				props.getJobCounters().get(FAILED).getCount());
	}

	private int getJobInProgressCount(final CloudNetworkAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS))
				.toList()
				.size();
	}

	private int getScheduledJobs(final CloudNetworkAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> !job.getValue().equals(PROCESSING))
				.toList()
				.size();
	}
}
