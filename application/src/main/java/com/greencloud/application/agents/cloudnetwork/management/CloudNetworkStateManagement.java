package com.greencloud.application.agents.cloudnetwork.management;

import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.greencloud.application.agents.cloudnetwork.constants.CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE;
import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.COUNT_JOB_ACCEPTED_LOG;
import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.COUNT_JOB_FINISH_LOG;
import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.COUNT_JOB_PROCESS_LOG;
import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.COUNT_JOB_START_LOG;
import static com.greencloud.application.utils.JobUtils.getJobSuccessRatio;
import static com.greencloud.application.utils.PowerUtils.getCurrentPowerInUse;
import static com.greencloud.application.utils.PowerUtils.getPowerPercent;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.STARTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PROCESSING;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import org.slf4j.Logger;

import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import com.greencloud.application.agents.AbstractStateManagement;
import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.agent.ServerData;
import com.greencloud.application.domain.job.JobCounter;
import com.greencloud.commons.domain.job.enums.JobExecutionResultEnum;
import com.gui.agents.CloudNetworkAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class stores methods used to manage the state of Cloud Network Agent
 */
public class CloudNetworkStateManagement extends AbstractStateManagement {

	private static final Logger logger = getLogger(CloudNetworkStateManagement.class);

	protected final CloudNetworkAgent cloudNetworkAgent;

	public CloudNetworkStateManagement(CloudNetworkAgent cloudNetworkAgent) {
		this.cloudNetworkAgent = cloudNetworkAgent;
	}

	/**
	 * Method retrieves list of owned servers that are active
	 *
	 * @return list of server AIDs
	 */
	public List<AID> getOwnedActiveServers() {
		return cloudNetworkAgent.getOwnedServers().entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.toList();
	}

	/**
	 * Method retrieves list of owned servers that are active and belong to given container group
	 *
	 * @return list of server AIDs
	 */
	public List<AID> getActiveServersForContainer(final String containerName) {
		return cloudNetworkAgent.getServerContainers().get(containerName).stream()
				.filter(server -> getOwnedActiveServers().contains(server))
				.toList();
	}

	/**
	 * Method returns comparator that enables to evaluate which Server proposal is better
	 *
	 * @return method comparator returns:
	 * <p> val > 0 - if the offer1 is better</p>
	 * <p> val = 0 - if both offers are equivalently good</p>
	 * <p> val < 0 - if the offer2 is better</p>
	 */
	public BiFunction<ACLMessage, ACLMessage, Integer> offerComparator() {
		return (offer1, offer2) -> {
			final int weight1 = cloudNetworkAgent.getWeightsForServersMap().get(offer1.getSender());
			final int weight2 = cloudNetworkAgent.getWeightsForServersMap().get(offer2.getSender());

			final Comparator<ServerData> comparator = (server1Data, server2Data) -> {
				final int powerDifference =
						(server2Data.getAvailablePower() * weight2) - (server1Data.getAvailablePower() * weight1);
				final double priceDifference =
						((server1Data.getServicePrice() * 1 / weight1) - (server2Data.getServicePrice() * 1 / weight2));

				return MAX_POWER_DIFFERENCE.isValidIntValue(powerDifference) ? (int) priceDifference : powerDifference;
			};

			return compareReceivedOffers(offer1, offer2, ServerData.class, comparator);
		};
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(
				FAILED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED).getCount())),
				ACCEPTED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED).getCount())),
				STARTED, new JobCounter(jobId -> {
					logger.info(COUNT_JOB_START_LOG, jobId, jobCounters.get(STARTED).getCount(),
							jobCounters.get(ACCEPTED).getCount());
					cloudNetworkAgent.getGuiController().updateActiveJobsCountByValue(1);
				}),
				FINISH,
				new JobCounter(jobId -> logger.info(COUNT_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH).getCount(),
						jobCounters.get(STARTED).getCount()))
		));
	}

	@Override
	public void updateGUI() {
		final CloudNetworkAgentNode cloudNetworkAgentNode = (CloudNetworkAgentNode) cloudNetworkAgent.getAgentNode();

		if (nonNull(cloudNetworkAgentNode)) {
			cloudNetworkAgentNode.updateClientNumber(getScheduledJobs());
			cloudNetworkAgentNode.updateJobsCount(getJobInProgressCount());
			cloudNetworkAgentNode.updateTraffic(getCurrentPowerInUse(cloudNetworkAgent.getNetworkJobs()));
			cloudNetworkAgentNode.updateCurrentJobSuccessRatio(getSuccessRatio());
		}
		saveMonitoringData();
	}

	/**
	 * Method returns current success ratio of given network region
	 *
	 * @return job execution success ratio
	 */
	public double getSuccessRatio() {
		return getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(), jobCounters.get(FAILED).getCount());
	}

	private void saveMonitoringData() {
		final int maxCapacity = (int) cloudNetworkAgent.getMaximumCapacity();
		final int traffic = getCurrentPowerInUse(cloudNetworkAgent.getNetworkJobs());
		final CloudNetworkMonitoringData cloudNetworkMonitoringData = ImmutableCloudNetworkMonitoringData.builder()
				.currentTraffic(getPowerPercent(traffic, maxCapacity))
				.availablePower((double) maxCapacity - traffic)
				.successRatio(getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(),
						jobCounters.get(FAILED).getCount()))
				.build();
		cloudNetworkAgent.writeMonitoringData(CLOUD_NETWORK_MONITORING, cloudNetworkMonitoringData);
	}

	private int getJobInProgressCount() {
		return cloudNetworkAgent.getNetworkJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS))
				.toList()
				.size();
	}

	private int getScheduledJobs() {
		return cloudNetworkAgent.getNetworkJobs().entrySet().stream()
				.filter(job -> !job.getValue().equals(PROCESSING))
				.toList()
				.size();
	}
}
