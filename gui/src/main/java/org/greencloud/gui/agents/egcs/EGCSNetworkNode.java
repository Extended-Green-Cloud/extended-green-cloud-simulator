package org.greencloud.gui.agents.egcs;

import static com.database.knowledge.types.DataType.JOB_ALLOCATION;
import static com.database.knowledge.types.DataType.JOB_ALLOCATION_ACCEPTANCE;
import static java.util.Collections.singletonList;
import static org.greencloud.commons.utils.allocation.AllocationMetrics.computeAllocationSuccessRatio;
import static org.greencloud.gui.websocket.WebSocketConnections.getAgentsWebSocket;
import static org.greencloud.gui.websocket.WebSocketConnections.getCloudNetworkSocket;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.greencloud.gui.messages.ImmutableIsActiveMessage;
import org.greencloud.gui.messages.ImmutableSetNumericValueMessage;
import org.greencloud.gui.messages.ImmutableUpdateAllocationResultMessage;
import org.jrba.agentmodel.domain.args.AgentArgs;
import org.jrba.agentmodel.domain.props.AgentProps;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.regionalmanager.JobAllocation;
import com.database.knowledge.domain.agent.regionalmanager.JobAllocationAcceptance;

/**
 * Class represents abstract generic agent node which is a part of regional manager
 */
public abstract class EGCSNetworkNode<T extends AgentArgs, E extends AgentProps> extends EGCSNode<T, E>
		implements Serializable {

	protected EGCSNetworkNode() {
	}

	/**
	 * Network agent node constructor
	 *
	 * @param nodeArgs arguments used to create agent node
	 * @param nodeType type of agent node
	 */
	protected EGCSNetworkNode(final T nodeArgs, final EGCSAgentType nodeType) {
		super(nodeArgs, nodeType);
	}

	/**
	 * Function updates the current traffic for given value
	 *
	 * @param traffic current traffic
	 */
	public void updateTraffic(final double traffic) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(traffic)
				.agentName(agentName)
				.type("SET_TRAFFIC")
				.build());
	}

	/**
	 * Function updates the information if the given network node is active
	 *
	 * @param isActive information if the network node is active
	 */
	public void updateIsActive(final boolean isActive) {
		if (!(this instanceof RMANode)) {
			getAgentsWebSocket().send(ImmutableIsActiveMessage.builder()
					.data(isActive)
					.agentName(agentName)
					.build());
		}
	}

	/**
	 * Function updates the number of currently executed jobs
	 *
	 * @param value new jobs count
	 */
	public void updateJobsCount(final int value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(value)
				.agentName(agentName)
				.type("SET_JOBS_COUNT")
				.build());
	}

	/**
	 * Function updates the number of jobs being on hold to given value
	 *
	 * @param value number of jobs that are on hold
	 */
	public void updateJobsOnHoldCount(final int value) {
		if (!(this instanceof RMANode)) {
			getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
					.data(value)
					.agentName(agentName)
					.type("SET_ON_HOLD_JOBS_COUNT")
					.build());
		}
	}

	/**
	 * Method stores in the database the information about the percentage success of allocated jobs.
	 *
	 * @param jobs           jobs to be allocated
	 * @param allocationTime time of job allocation
	 * @param allocatedJobs  jobs that were successfully allocated
	 * @param agentName      name of the agent
	 */
	public void reportJobAllocationPercentage(final List<ClientJob> jobs, final Map<String, List<String>> allocatedJobs,
			final long allocationTime, final String agentName) {
		final double allocationSuccessRatio = computeAllocationSuccessRatio(jobs, allocatedJobs);
		final List<JobAllocation> allocationData =
				databaseClient.readMonitoringDataForDataTypes(singletonList(JOB_ALLOCATION), 10000).stream()
						.map(AgentData::monitoringData)
						.map(JobAllocation.class::cast)
						.toList();
		final long dataSize = allocationData.size();
		final double currentSumSuccessRatio = allocationData.stream()
				.mapToDouble(JobAllocation::jobAllocationPercentage)
				.sum();
		final double currentSumAllocationTime = allocationData.stream()
				.mapToDouble(JobAllocation::allocationTime)
				.sum();

		getCloudNetworkSocket().send(ImmutableUpdateAllocationResultMessage.builder()
				.allocationSuccess(100 * (allocationSuccessRatio + currentSumSuccessRatio) / (dataSize + 1))
				.allocationTime((allocationTime + currentSumAllocationTime) / (dataSize + 1))
				.build());

		writeMonitoringData(JOB_ALLOCATION, new JobAllocation(allocationSuccessRatio, allocationTime), agentName);
	}

	/**
	 * Method stores in the database the information about the percentage of accepted allocated jobs
	 *
	 * @param allocationAcceptanceRatio ratio of jobs accepted by allocated executors
	 * @param agentName                 name of the agent
	 */
	public void reportJobAllocationAcceptance(final double allocationAcceptanceRatio, final String agentName) {
		final List<JobAllocationAcceptance> allocationData =
				databaseClient.readMonitoringDataForDataTypes(singletonList(JOB_ALLOCATION_ACCEPTANCE), 10000)
						.stream()
						.map(AgentData::monitoringData)
						.map(JobAllocationAcceptance.class::cast)
						.toList();
		final long dataSize = allocationData.size();
		final double currentSumAcceptanceRatio = allocationData.stream()
				.mapToDouble(JobAllocationAcceptance::jobAllocationAcceptance)
				.sum();

		getCloudNetworkSocket().send(ImmutableSetNumericValueMessage.builder()
				.data(100 * (currentSumAcceptanceRatio + allocationAcceptanceRatio) / (dataSize + 1))
				.agentName(getAgentName())
				.type("UPDATE_ALLOCATION_ACCEPTANCE_RATIO")
				.build());

		writeMonitoringData(JOB_ALLOCATION_ACCEPTANCE, new JobAllocationAcceptance(allocationAcceptanceRatio),
				agentName);
	}

	/**
	 * Function updates the current job success ratio of a network agent
	 *
	 * @param value new success ratio
	 */
	public void updateCurrentJobSuccessRatio(final double value) {
		getAgentsWebSocket().send(ImmutableSetNumericValueMessage.builder()
				.type("SET_JOB_SUCCESS_RATIO")
				.agentName(agentName)
				.data(value * 100)
				.build());
	}
}
