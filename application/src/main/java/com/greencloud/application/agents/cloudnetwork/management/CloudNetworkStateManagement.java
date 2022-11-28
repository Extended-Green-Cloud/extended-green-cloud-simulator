package com.greencloud.application.agents.cloudnetwork.management;

import static com.greencloud.application.agents.cloudnetwork.management.logs.CloudNetworkManagementLog.*;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.GUIUtils.announceFinishedJob;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PROCESSING;
import static com.greencloud.commons.job.JobResultType.*;
import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.greencloud.commons.job.JobResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.gui.agents.CloudNetworkAgentNode;

/**
 * Set of utilities used to manage the state of the cloud network
 */
public class CloudNetworkStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(CloudNetworkStateManagement.class);
	private final CloudNetworkAgent cloudNetworkAgent;
	private final ConcurrentMap<JobResultType, Long> jobCounters;

	public CloudNetworkStateManagement(CloudNetworkAgent cloudNetworkAgent) {
		this.cloudNetworkAgent = cloudNetworkAgent;
		this.jobCounters = Arrays.stream(JobResultType.values())
				.collect(Collectors.toConcurrentMap(result -> result, status -> 0L));
	}

	/**
	 * Method calculates the power in use at the given moment
	 *
	 * @return current power in use
	 */
	public int getCurrentPowerInUse() {
		return cloudNetworkAgent.getNetworkJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(ExecutionJobStatusEnum.IN_PROGRESS))
				.mapToInt(job -> job.getKey().getPower())
				.sum();
	}

	/**
	 * Method increments the counter of jobs
	 *
	 * @param jobId job identifier
	 * @param type  type of counter to increment
	 */
	public void incrementJobCounter(final String jobId, final JobResultType type) {
		MDC.put(MDC_JOB_ID, jobId);
		jobCounters.computeIfPresent(type, (key, val) ->  val += 1);

		switch (type) {
			case FAILED -> logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED));
			case ACCEPTED -> logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED));
			case STARTED -> {
				logger.info(COUNT_JOB_START_LOG, jobId, jobCounters.get(STARTED),
						jobCounters.get(ACCEPTED));
				cloudNetworkAgent.getGuiController().updateActiveJobsCountByValue(1);
			}
			case FINISH -> {
				logger.info(COUNT_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH), jobCounters.get(STARTED));
				announceFinishedJob(cloudNetworkAgent);
			}
		}
		updateCloudNetworkGUI();
	}

	public Map<JobResultType, Long> getJobCounters() {
		return jobCounters;
	}

	private void updateCloudNetworkGUI() {
		final CloudNetworkAgentNode cloudNetworkAgentNode = (CloudNetworkAgentNode) cloudNetworkAgent.getAgentNode();

		if (nonNull(cloudNetworkAgentNode)) {
			cloudNetworkAgentNode.updateClientNumber(getScheduledJobs());
			cloudNetworkAgentNode.updateJobsCount(getJobInProgressCount());
			cloudNetworkAgentNode.updateTraffic(getCurrentPowerInUse());
		}
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
