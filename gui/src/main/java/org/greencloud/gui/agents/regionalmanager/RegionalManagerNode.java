package org.greencloud.gui.agents.regionalmanager;

import static com.database.knowledge.domain.agent.DataType.REGIONAL_MANAGER_MONITORING;
import static java.util.Optional.ofNullable;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.util.Map;
import java.util.Optional;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.args.agent.regionalmanager.node.RegionalManagerNodeArgs;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.utils.job.JobUtils;
import org.greencloud.gui.agents.egcs.EGCSNetworkNode;
import org.greencloud.gui.messages.ImmutableSetNumericValueMessage;
import org.greencloud.gui.messages.ImmutableUpdateDefaultResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateSingleValueMessage;
import org.jrba.environment.domain.ExternalEvent;

import com.database.knowledge.domain.agent.regionalmanager.ImmutableRegionalManagerMonitoringData;
import com.database.knowledge.domain.agent.regionalmanager.RegionalManagerMonitoringData;

/**
 * Agent node class representing the regional manager
 */
public class RegionalManagerNode extends EGCSNetworkNode<RegionalManagerNodeArgs, RegionalManagerAgentProps> {

	/**
	 * Regional manager node constructor
	 *
	 * @param regionalManagerArgs node arguments
	 */
	public RegionalManagerNode(final RegionalManagerNodeArgs regionalManagerArgs) {
		super(regionalManagerArgs, EGCSAgentType.REGIONAL_MANAGER);
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

	public Optional<ExternalEvent> getEvent() {
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
	 * Method updates resources owned by RMA
	 *
	 * @param newResources new RMA resources
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
	public void updateGUI(final RegionalManagerAgentProps agentProps) {
		updateResources(agentProps.getInUseResources());
		updateClientNumber(getScheduledJobs(agentProps));
		updateJobsCount(getJobInProgressCount(agentProps));
		updateCurrentJobSuccessRatio(getSuccessRatio(agentProps));
	}

	@Override
	public void saveMonitoringData(final RegionalManagerAgentProps props) {
		final RegionalManagerMonitoringData regionalManagerMonitoringData = ImmutableRegionalManagerMonitoringData.builder()
				.successRatio(getSuccessRatio(props))
				.build();
		writeMonitoringData(REGIONAL_MANAGER_MONITORING, regionalManagerMonitoringData, props.getAgentName());
	}

	private double getSuccessRatio(final RegionalManagerAgentProps props) {
		return JobUtils.getJobSuccessRatio(props.getJobCounters().get(JobExecutionResultEnum.ACCEPTED).getCount(),
				props.getJobCounters().get(JobExecutionResultEnum.FAILED).getCount());
	}

	private int getJobInProgressCount(final RegionalManagerAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobExecutionStatusEnum.IN_PROGRESS))
				.toList()
				.size();
	}

	private int getScheduledJobs(final RegionalManagerAgentProps agentProps) {
		return agentProps.getNetworkJobs().entrySet().stream()
				.filter(job -> !job.getValue().equals(JobExecutionStatusEnum.PROCESSING))
				.toList()
				.size();
	}
}
