package com.greencloud.application.agents.server.management;

import static com.greencloud.application.agents.server.domain.ServerPowerSourceType.ALL;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.application.domain.job.JobStatusEnum.JOB_ON_HOLD;
import static com.greencloud.application.utils.JobMapUtils.*;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.domain.ServerPowerSourceType;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.utils.AlgorithmUtils;
import com.gui.agents.ServerAgentNode;

/**
 * Set of utilities used to manage the internal state of the server agent
 */
public class ServerStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(ServerStateManagement.class);

	protected final AtomicInteger uniqueStartedJobs;
	protected final AtomicInteger uniqueFinishedJobs;
	protected final AtomicInteger startedJobsInstances;
	protected final AtomicInteger finishedJobsInstances;
	private final ServerAgent serverAgent;

	public ServerStateManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
		this.uniqueStartedJobs = new AtomicInteger(0);
		this.uniqueFinishedJobs = new AtomicInteger(0);
		this.startedJobsInstances = new AtomicInteger(0);
		this.finishedJobsInstances = new AtomicInteger(0);
	}

	/**
	 * Method computes the available capacity (of given type) for the specified time frame.
	 *
	 * @param startDate       starting date
	 * @param endDate         end date
	 * @param jobToExclude    (optional) job which will be excluded from the power calculation
	 * @param powerSourceType type of the source which is being used to power-up the job
	 *                        (if not provided then type is ALL)
	 * @return available power
	 */
	public synchronized int getAvailableCapacity(final Instant startDate, final Instant endDate,
			final JobInstanceIdentifier jobToExclude, final ServerPowerSourceType powerSourceType) {
		final Set<JobStatusEnum> statuses = Objects.isNull(powerSourceType) ?
				ALL.getJobStatuses() :
				powerSourceType.getJobStatuses();
		final Set<ClientJob> jobsOfInterest = serverAgent.getServerJobs().keySet().stream()
				.filter(job -> Objects.isNull(jobToExclude) || !JobMapper.mapToJobInstanceId(job).equals(jobToExclude))
				.filter(job -> statuses.contains(serverAgent.getServerJobs().get(job)))
				.collect(Collectors.toSet());
		final int maxUsedPower =
				AlgorithmUtils.getMaximumUsedPowerDuringTimeStamp(jobsOfInterest, startDate, endDate);
		return serverAgent.getCurrentMaximumCapacity() - maxUsedPower;
	}

	/**
	 * Method increments the count of started jobs
	 *
	 * @param jobId unique job identifier
	 */
	public void incrementStartedJobs(final String jobId) {
		if (isJobUnique(serverAgent.getServerJobs(), jobId)) {
			uniqueStartedJobs.getAndAdd(1);
			logger.info("Started job {}. Number of unique started jobs is {}", jobId, uniqueStartedJobs);
		}
		startedJobsInstances.getAndAdd(1);
		logger.info("Started job instance {}. Number of started job instances is {}", jobId, startedJobsInstances);
		updateServerGUI();
	}

	/**
	 * Method increments the count of finished jobs
	 *
	 * @param jobId unique identifier of the job
	 */
	public void incrementFinishedJobs(final String jobId) {
		MDC.put(MDC_JOB_ID, jobId);
		if (isJobUnique(serverAgent.getServerJobs(), jobId)) {
			uniqueFinishedJobs.getAndAdd(1);
			logger.info("Finished job {}. Number of unique finished jobs is {} out of {} started", jobId,
					uniqueFinishedJobs, uniqueStartedJobs);
		}
		finishedJobsInstances.getAndAdd(1);
		logger.info("Finished job instance {}. Number of finished job instances is {} out of {} started", jobId,
				finishedJobsInstances, startedJobsInstances);
		updateServerGUI();
	}

	/**
	 * Method changes the server's maximum capacity
	 *
	 * @param newMaximumCapacity new maximum capacity value
	 */
	public void updateMaximumCapacity(final int newMaximumCapacity) {
		serverAgent.setCurrentMaximumCapacity(newMaximumCapacity);

		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();
		if (Objects.nonNull(serverAgentNode)) {
			serverAgentNode.updateMaximumCapacity(serverAgent.getCurrentMaximumCapacity(),
					getCurrentPowerInUseForServer());
		}
	}

	/**
	 * Method updates the information on the server GUI
	 */
	public void updateServerGUI() {
		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();

		if (Objects.nonNull(serverAgentNode)) {
			serverAgentNode.updateMaximumCapacity(serverAgent.getCurrentMaximumCapacity(),
					getCurrentPowerInUseForServer());
			serverAgentNode.updateJobsCount(getJobCount(serverAgent.getServerJobs()));
			serverAgentNode.updateClientNumber(getClientNumber());
			serverAgentNode.updateIsActive(getIsActiveState());
			serverAgentNode.updateTraffic(getCurrentPowerInUseForServer());
			serverAgentNode.updateBackUpTraffic(getCurrentBackUpPowerInUseForServer());
			serverAgentNode.updateJobsOnHoldCount(getOnHoldJobsCount());
		}
	}

	/**
	 * Method updates the client number
	 */
	public void updateClientNumberGUI() {
		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();

		if (Objects.nonNull(serverAgentNode)) {
			serverAgentNode.updateClientNumber(getClientNumber());
		}
	}

	public AtomicInteger getUniqueStartedJobs() {
		return uniqueStartedJobs;
	}

	public AtomicInteger getUniqueFinishedJobs() {
		return uniqueFinishedJobs;
	}

	public AtomicInteger getStartedJobsInstances() {
		return startedJobsInstances;
	}

	public AtomicInteger getFinishedJobsInstances() {
		return finishedJobsInstances;
	}

	private int getClientNumber() {
		return serverAgent.getGreenSourceForJobMap().size();
	}

	private int getCurrentPowerInUseForServer() {
		return serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS) && isWithinTimeStamp(job.getKey().getStartTime(),
						job.getKey().getEndTime(), getCurrentTime())).mapToInt(job -> job.getKey().getPower()).sum();
	}

	private int getCurrentBackUpPowerInUseForServer() {
		return serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(IN_PROGRESS_BACKUP_ENERGY) && isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.mapToInt(job -> job.getKey().getPower()).sum();
	}

	private int getOnHoldJobsCount() {
		return serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> JOB_ON_HOLD.contains(job.getValue()) && isWithinTimeStamp(job.getKey().getStartTime(),
						job.getKey().getEndTime(), getCurrentTime())).toList().size();
	}

	private boolean getIsActiveState() {
		return getCurrentPowerInUseForServer() > 0 || getCurrentBackUpPowerInUseForServer() > 0;
	}
}
