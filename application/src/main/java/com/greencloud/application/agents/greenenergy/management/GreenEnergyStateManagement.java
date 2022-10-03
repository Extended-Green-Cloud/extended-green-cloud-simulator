package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.DUPLICATED_POWER_JOB_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.DUPLICATED_POWER_JOB_START_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.UNIQUE_POWER_JOB_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.UNIQUE_POWER_JOB_START_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.JOB_ON_HOLD;
import static com.greencloud.application.utils.JobMapUtils.getJobCount;
import static com.greencloud.application.utils.JobMapUtils.isJobUnique;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.PowerJob;
import com.gui.agents.GreenEnergyAgentNode;

/**
 * Set of methods used to manage the internal state of the green energy agent
 */
public class GreenEnergyStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyStateManagement.class);
	protected final AtomicInteger uniqueStartedJobs;
	protected final AtomicInteger uniqueFinishedJobs;
	protected final AtomicInteger startedJobsInstances;
	protected final AtomicInteger finishedJobsInstances;
	private final GreenEnergyAgent greenEnergyAgent;

	/**
	 * Default constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyStateManagement(GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
		this.uniqueStartedJobs = new AtomicInteger(0);
		this.uniqueFinishedJobs = new AtomicInteger(0);
		this.startedJobsInstances = new AtomicInteger(0);
		this.finishedJobsInstances = new AtomicInteger(0);
	}

	/**
	 * Method increments the count of started jobs
	 *
	 * @param jobId unique job identifier
	 */
	public void incrementStartedJobs(final String jobId) {
		MDC.put(MDC_JOB_ID, jobId);
		if (isJobUnique(greenEnergyAgent.getPowerJobs(), jobId)) {
			uniqueStartedJobs.getAndAdd(1);
			logger.info(UNIQUE_POWER_JOB_START_LOG, jobId, uniqueStartedJobs);
		}
		startedJobsInstances.getAndAdd(1);
		logger.info(DUPLICATED_POWER_JOB_START_LOG, jobId, startedJobsInstances);
		updateGreenSourceGUI();
	}

	/**
	 * Method increments the count of finished jobs
	 *
	 * @param jobId unique identifier of the job
	 */
	public void incrementFinishedJobs(final String jobId) {
		MDC.put(MDC_JOB_ID, jobId);
		if (isJobUnique(greenEnergyAgent.getPowerJobs(), jobId)) {
			uniqueFinishedJobs.getAndAdd(1);
			logger.info(UNIQUE_POWER_JOB_FINISH_LOG, jobId,
					uniqueFinishedJobs, uniqueStartedJobs);
		}
		finishedJobsInstances.getAndAdd(1);
		logger.info(DUPLICATED_POWER_JOB_FINISH_LOG, jobId,
				finishedJobsInstances, startedJobsInstances);
		updateGreenSourceGUI();
	}

	/**
	 * Finds distinct start and end times of taken {@link PowerJob}s including the candidate job
	 *
	 * @param candidateJob job defining the search time window
	 * @return list of all start and end times
	 */
	public List<Instant> getJobsTimetable(PowerJob candidateJob) {
		var validJobs = greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(entry -> ACCEPTED_JOB_STATUSES.contains(entry.getValue()))
				.map(Entry::getKey)
				.toList();
		return Stream.concat(
						Stream.of(candidateJob.getStartTime(), candidateJob.getEndTime()),
						Stream.concat(
								validJobs.stream().map(PowerJob::getStartTime),
								validJobs.stream().map(PowerJob::getEndTime)))
				.distinct()
				.toList();
	}

	/**
	 * Method updates the information on the green source GUI
	 */
	public void updateGreenSourceGUI() {
		final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();

		if (nonNull(greenEnergyAgentNode)) {
			greenEnergyAgentNode.updateMaximumCapacity(greenEnergyAgent.managePower().getMaximumCapacity(),
					greenEnergyAgent.managePower().getCurrentPowerInUseForGreenSource());
			greenEnergyAgentNode.updateJobsCount(getJobCount(greenEnergyAgent.getPowerJobs()));
			greenEnergyAgentNode.updateJobsOnHoldCount(getOnHoldJobCount());
			greenEnergyAgentNode.updateIsActive(getIsActiveState());
			greenEnergyAgentNode.updateTraffic(greenEnergyAgent.managePower().getCurrentPowerInUseForGreenSource());
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

	private int getOnHoldJobCount() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> JOB_ON_HOLD.contains(job.getValue())
						&& isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.toList()
				.size();
	}

	private boolean getIsActiveState() {
		return greenEnergyAgent.managePower().getCurrentPowerInUseForGreenSource() > 0 || getOnHoldJobCount() > 0;
	}
}
