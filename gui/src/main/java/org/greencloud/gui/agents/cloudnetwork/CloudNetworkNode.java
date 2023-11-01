package org.greencloud.gui.agents.cloudnetwork;

import static java.util.Optional.ofNullable;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.util.Map;
import java.util.Optional;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.args.agent.cloudnetwork.node.CloudNetworkNodeArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.utils.job.JobUtils;
import org.greencloud.gui.agents.egcs.EGCSNetworkNode;
import org.greencloud.gui.event.AbstractEvent;
import org.greencloud.gui.messages.ImmutableSetNumericValueMessage;
import org.greencloud.gui.messages.ImmutableUpdateDefaultResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateSingleValueMessage;

import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;

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
		super(cloudNetworkArgs, AgentType.CLOUD_NETWORK);
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
	 * Method updates resources owned by CNA
	 *
	 * @param newResources new CNA resources
	 */
	public void updateResourceMap(final Map<String, Resource> newResources) {
		getCloudNetworkSocket().send(ImmutableUpdateDefaultResourcesMessage.builder()
				.agentName(agentName)
				.resources(newResources)
				.build());
	}

	/**
	 * Function updates current in-use resources
	 *
	 * @param resources currently utilized resources
	 */
	public void updateResources(final Map<String, Resource> resources) {
		getAgentsWebSocket().send(ImmutableUpdateResourcesMessage.builder()
				.resources(resources)
				.agentName(agentName)
				.build());
	}

	/**
	 * Method updates GUI of given agent node
	 *
	 * @param agentProps current properties of an agent
	 */
	@Override
	public void updateGUI(final CloudNetworkAgentProps agentProps) {
		updateResources(agentProps.getInUseResources());
		updateClientNumber(getScheduledJobs(agentProps));
		updateJobsCount(getJobInProgressCount(agentProps));
		updateCurrentJobSuccessRatio(getSuccessRatio(agentProps));
	}

	@Override
	public void saveMonitoringData(final CloudNetworkAgentProps props) {
		final CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(getSuccessRatio(props))
				.build();
		writeMonitoringData(DataType.CLOUD_NETWORK_MONITORING, cloudNetworkMonitoringData, props.getAgentName());
	}

	private double getSuccessRatio(final CloudNetworkAgentProps props) {
		return JobUtils.getJobSuccessRatio(props.getJobCounters().get(JobExecutionResultEnum.ACCEPTED).getCount(),
				props.getJobCounters().get(JobExecutionResultEnum.FAILED).getCount());
	}

	private int getJobInProgressCount(final CloudNetworkAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobExecutionStatusEnum.IN_PROGRESS))
				.toList()
				.size();
	}

	private int getScheduledJobs(final CloudNetworkAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> !job.getValue().equals(JobExecutionStatusEnum.PROCESSING))
				.toList()
				.size();
	}
}
