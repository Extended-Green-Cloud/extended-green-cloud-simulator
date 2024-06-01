package org.greencloud.gui.agents.server;

import static com.database.knowledge.types.DataType.SERVER_MONITORING;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.args.agent.server.node.ServerNodeArgs;
import org.greencloud.commons.constants.resource.ResourceTypesConstants;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.utils.job.JobUtils;
import org.greencloud.gui.agents.egcs.EGCSNetworkNode;
import org.greencloud.gui.messages.ImmutableDisableServerMessage;
import org.greencloud.gui.messages.ImmutableEnableServerMessage;
import org.greencloud.gui.messages.ImmutableSetNumericValueMessage;
import org.greencloud.gui.messages.ImmutableUpdateDefaultResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateResourcesMessage;
import org.greencloud.gui.messages.ImmutableUpdateServerMaintenanceMessage;
import org.greencloud.gui.messages.ImmutableUpdateSingleValueMessage;
import org.jrba.environment.domain.ExternalEvent;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.NetworkComponentMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.types.DataType;

import jade.util.leap.Serializable;

/**
 * Agent node class representing the server
 */
public class ServerNode extends EGCSNetworkNode<ServerNodeArgs, ServerAgentProps> implements Serializable {

	public ServerNode() {
		super();
	}

	/**
	 * Server node constructor
	 *
	 * @param serverNodeArgs arguments of given server node
	 */
	public ServerNode(ServerNodeArgs serverNodeArgs) {
		super(serverNodeArgs, EGCSAgentType.SERVER);
	}

	/**
	 * Function updates the current back-up traffic to given value
	 *
	 * @param backUpPowerInUse current power in use coming from back-up energy
	 */
	public void updateBackUpTraffic(final double backUpPowerInUse) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(backUpPowerInUse)
				.agentName(agentName)
				.type("SET_SERVER_BACK_UP_TRAFFIC")
				.build());
	}

	/**
	 * Function updates current in-use resources
	 *
	 * @param resources              currently utilized resources
	 * @param powerConsumption       current power consumption
	 * @param powerConsumptionBackUp current back-up power consumption
	 */
	public void updateResources(final Map<String, Resource> resources, final double powerConsumption,
			final double powerConsumptionBackUp) {
		getAgentsWebSocket().send(ImmutableUpdateResourcesMessage.builder()
				.resources(resources)
				.powerConsumption(powerConsumption)
				.powerConsumptionBackUp(powerConsumptionBackUp)
				.agentName(agentName)
				.build());
	}

	/**
	 * Function updates initial server resources
	 *
	 * @param resources default resource characteristics
	 */
	public void updateDefaultResources(final Map<String, Resource> resources) {
		getAgentsWebSocket().send(ImmutableUpdateDefaultResourcesMessage.builder()
				.resources(resources)
				.agentName(agentName)
				.build());
	}

	/**
	 * Function confirms that maintenance was initiated in server
	 */
	public void confirmMaintenanceInServer() {
		getAgentsWebSocket().send(ImmutableUpdateServerMaintenanceMessage.builder()
				.agentName(agentName)
				.result(true)
				.state("processDataInServer")
				.build());
	}

	/**
	 * Function sends the result of adaptation of server resources in RMA
	 */
	public void sendResultOfServerMaintenanceInRMA(final boolean result) {
		getAgentsWebSocket().send(ImmutableUpdateServerMaintenanceMessage.builder()
				.agentName(agentName)
				.result(result)
				.state("informationInManager")
				.build());
	}

	/**
	 * Function confirms that maintenance completed successfully
	 */
	public void confirmSuccessfulMaintenance() {
		getAgentsWebSocket().send(ImmutableUpdateServerMaintenanceMessage.builder()
				.agentName(agentName)
				.result(true)
				.state("maintenanceCompleted")
				.build());
	}

	/**
	 * Function updates the number of clients
	 *
	 * @param value new clients count
	 */
	public void updateClientNumber(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_CLIENT_NUMBER")
				.build());
	}

	/**
	 * Function disables the server
	 */
	public void disableServer() {
		getAgentsWebSocket().send(ImmutableDisableServerMessage.builder()
				.rma(nodeArgs.getRegionalManagerAgent())
				.server(agentName)
				.cpu(nodeArgs.getResources().get(ResourceTypesConstants.CPU).getAmount())
				.build());
	}

	/**
	 * Function enables the server
	 */
	public void enableServer() {
		getAgentsWebSocket().send(ImmutableEnableServerMessage.builder()
				.rma(nodeArgs.getRegionalManagerAgent())
				.rma(nodeArgs.getRegionalManagerAgent())
				.server(agentName)
				.cpu(nodeArgs.getResources().get(ResourceTypesConstants.CPU).getAmount())
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
	 * Method returns current success ratio of the component.
	 *
	 * @return currently recorded success ratio
	 */
	public double getComponentSuccessRatio(final String agentAID) {
		return databaseClient.readLastMonitoringDataForDataTypes(singletonList(SERVER_MONITORING)).stream()
				.filter(agentData -> agentData.aid().equals(agentAID))
				.map(AgentData::monitoringData)
				.map(ServerMonitoringData.class::cast)
				.findFirst()
				.map(NetworkComponentMonitoringData::getSuccessRatio)
				.orElse(1.0D);
	}

	public Optional<ExternalEvent> getEvent() {
		return Optional.ofNullable(eventsQueue.poll());
	}

	@Override
	public void updateGUI(final ServerAgentProps props) {
		final double successRatio = JobUtils.getJobSuccessRatio(props.getJobCounters().get(ACCEPTED).getCount(),
				props.getJobCounters().get(FAILED).getCount());
		final double backUpTraffic = props.getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY));
		final Map<String, Resource> inUseResources = props.getInUseResources();
		final double powerConsumption = props.getCurrentPowerConsumption();
		final double powerConsumptionBackUp = props.getCurrentPowerConsumptionBackUp();
		final ConcurrentMap<ClientJob, JobExecutionStatusEnum> jobs = props.getServerJobs();

		updateTraffic(props.getCPUUsage(null));
		updateBackUpTraffic(backUpTraffic);
		updateResources(inUseResources, powerConsumption, powerConsumptionBackUp);
		updateJobsCount(JobUtils.getJobCount(jobs));
		updateJobsOnHoldCount(JobUtils.getJobCount(jobs, JobExecutionStatusEnum.JOB_ON_HOLD_STATUSES));
		updateClientNumber(JobUtils.getJobCount(jobs, JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES));
		updateIsActive(getIsActiveState(props));
		updateCurrentJobSuccessRatio(successRatio);
		saveMonitoringData(props);
	}

	/**
	 * Method saves monitoring data of given agent node to database
	 *
	 * @param props current properties of Server agent
	 */
	@Override
	public void saveMonitoringData(final ServerAgentProps props) {
		final double greenPowerUsage = props.getCPUUsage(null);
		final double backPowerUsage = props.getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY));
		final int jobsNo = props.getServerJobs().size() - JobUtils.getJobCount(props.getServerJobs(),
				JobExecutionStatusEnum.JOB_ON_HOLD_STATUSES);
		final double successRatio = JobUtils.getJobSuccessRatio(props.getJobCounters().get(
						ACCEPTED).getCount(),
				props.getJobCounters().get(FAILED).getCount());

		final ServerMonitoringData serverMonitoringData = ImmutableServerMonitoringData.builder()
				.idlePowerConsumption(props.getIdlePowerConsumption())
				.currentPowerConsumption(props.getCurrentPowerConsumption())
				.currentTraffic(greenPowerUsage)
				.currentBackUpPowerTraffic(backPowerUsage)
				.serverJobs(jobsNo)
				.successRatio(successRatio)
				.isDisabled(props.isDisabled())
				.build();

		writeMonitoringData(DataType.SERVER_MONITORING, serverMonitoringData, props.getAgentName());
	}

	private boolean getIsActiveState(final ServerAgentProps props) {
		return props.getCPUUsage(null) > 0 || props.getCPUUsage(singleton(
				IN_PROGRESS_BACKUP_ENERGY)) > 0;
	}
}
