package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INTERVAL_LENGTH_MIN;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.AVERAGE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.CURRENT_AVAILABLE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.DUPLICATED_POWER_JOB_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.DUPLICATED_POWER_JOB_START_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.JOB_ON_HOLD_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.RUNNING_JOB_STATUSES;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceIdWithRealTime;
import static com.greencloud.application.utils.AlgorithmUtils.computeIncorrectMaximumValProbability;
import static com.greencloud.application.utils.AlgorithmUtils.getMinimalAvailablePowerDuringTimeStamp;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static java.lang.Math.min;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.application.mapper.JobMapper;
import com.gui.agents.GreenEnergyAgentNode;

/**
 * Set of methods used to manage the internal state of the green energy agent
 */
public class GreenEnergyStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyStateManagement.class);
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
		this.startedJobsInstances = new AtomicInteger(0);
		this.finishedJobsInstances = new AtomicInteger(0);
	}

	/**
	 * Method retrieves the job by the job id from job map
	 *
	 * @param jobId job identifier
	 * @return job or null if job is not found
	 */
	public PowerJob getJobById(final String jobId) {
		return greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> job.getJobId().equals(jobId))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and start time from job map
	 *
	 * @param jobId     job identifier
	 * @param startTime job start time
	 * @return job
	 */
	public PowerJob getJobByIdAndStartDate(final String jobId, final Instant startTime) {
		return greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> job.getJobId().equals(jobId) && job.getStartTime().equals(startTime))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and start time from job map
	 *
	 * @param jobInstanceId unique identifier of the job instance
	 * @return job
	 */
	public PowerJob getJobByIdAndStartDate(final JobInstanceIdentifier jobInstanceId) {
		return greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> job.getJobId().equals(jobInstanceId.getJobId())
						&& job.getStartTime().equals(jobInstanceId.getStartTime()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Method increments the count of started jobs
	 *
	 * @param jobInstanceId job identifier
	 */
	public void incrementStartedJobs(final JobInstanceIdentifier jobInstanceId) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		startedJobsInstances.getAndAdd(1);
		logger.info(DUPLICATED_POWER_JOB_START_LOG, mapToJobInstanceIdWithRealTime(jobInstanceId),
				startedJobsInstances);
		updateGreenSourceGUI();
	}

	/**
	 * Method increments the count of finished jobs
	 *
	 * @param jobInstanceId identifier of the job
	 */
	public void incrementFinishedJobs(final JobInstanceIdentifier jobInstanceId) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		finishedJobsInstances.getAndAdd(1);
		logger.info(DUPLICATED_POWER_JOB_FINISH_LOG, mapToJobInstanceIdWithRealTime(jobInstanceId),
				finishedJobsInstances, startedJobsInstances);
	}

	/**
	 * Method changes the green source's maximum capacity
	 *
	 * @param newMaximumCapacity new maximum capacity value
	 */
	public void updateMaximumCapacity(final int newMaximumCapacity) {
		greenEnergyAgent.manageGreenPower().setCurrentMaximumCapacity(newMaximumCapacity);
		final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();

		if (nonNull(greenEnergyAgentNode)) {
			greenEnergyAgentNode.updateMaximumCapacity(greenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity(),
					getCurrentPowerInUseForGreenSource());
		}
	}

	/**
	 * Method creates new instances for given power job which will be affected by the power shortage.
	 * If the power shortage will begin after the start of job execution -> job will be divided into 2
	 *
	 * Example:
	 * Job1 (start: 08:00, finish: 10:00)
	 * Power shortage start: 09:00
	 *
	 * Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage
	 * Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage
	 *
	 * @param powerJob           affected power job
	 * @param powerShortageStart time when power shortage starts
	 */
	public PowerJob dividePowerJobForPowerShortage(final PowerJob powerJob, final Instant powerShortageStart) {
		if (powerShortageStart.isAfter(powerJob.getStartTime())) {
			final PowerJob affectedPowerJobInstance = JobMapper.mapToJobNewStartTime(powerJob, powerShortageStart);
			final PowerJob notAffectedPowerJobInstance = JobMapper.mapToJobNewEndTime(powerJob, powerShortageStart);
			final JobStatusEnum currentJobStatus = greenEnergyAgent.getPowerJobs().get(powerJob);

			greenEnergyAgent.getPowerJobs().remove(powerJob);
			greenEnergyAgent.getPowerJobs().put(affectedPowerJobInstance, JobStatusEnum.ON_HOLD_TRANSFER);
			greenEnergyAgent.getPowerJobs().put(notAffectedPowerJobInstance, currentJobStatus);
			final Date endDate = Date.from(affectedPowerJobInstance.getEndTime().plusMillis(MAX_ERROR_IN_JOB_FINISH));
			greenEnergyAgent.addBehaviour(new HandleManualPowerSupplyFinish(greenEnergyAgent, endDate,
					mapToJobInstanceId(affectedPowerJobInstance)));
			updateGreenSourceGUI();
			return affectedPowerJobInstance;
		} else {
			greenEnergyAgent.getPowerJobs().replace(powerJob, JobStatusEnum.ON_HOLD_TRANSFER);
			updateGreenSourceGUI();
			return powerJob;
		}
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
						Stream.of(
								convertToRealTime(candidateJob.getStartTime()),
								convertToRealTime(candidateJob.getEndTime())),
						Stream.concat(
								validJobs.stream().map(job -> convertToRealTime(job.getStartTime())),
								validJobs.stream().map(job -> convertToRealTime(job.getEndTime()))))
				.distinct()
				.toList();
	}

	/**
	 * Computes power available during computation of the job being processed
	 *
	 * @param powerJob job of interest
	 * @param weather  monitoring data with com.greencloud.application.weather for requested timetable
	 * @param isNewJob flag indicating whether job of interest is a processed new job
	 * @return available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailablePowerForJob(final PowerJob powerJob,
			final MonitoringData weather, final boolean isNewJob) {
		final Set<JobStatusEnum> jobStatuses = isNewJob ? ACCEPTED_JOB_STATUSES : ACTIVE_JOB_STATUSES;
		final Set<PowerJob> powerJobsOfInterest = greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> jobStatuses.contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(JobMapper::mapToPowerJobRealTime)
				.collect(Collectors.toSet());
		final Instant realJobStartTime = convertToRealTime(powerJob.getStartTime());
		final Instant realJobEndTime = convertToRealTime(powerJob.getEndTime());

		final double availablePower =
				getMinimalAvailablePowerDuringTimeStamp(
						powerJobsOfInterest,
						realJobStartTime,
						realJobEndTime,
						INTERVAL_LENGTH_MIN,
						greenEnergyAgent.manageGreenPower(),
						weather);
		final String power = String.format("%.2f", availablePower);

		MDC.put(MDC_JOB_ID, powerJob.getJobId());
		logger.info(AVERAGE_POWER_LOG, greenEnergyAgent.getEnergyType(), power,
				realJobStartTime, realJobEndTime);

		return availablePower <= 0 ?
				Optional.empty() :
				Optional.of(availablePower);
	}

	/**
	 * Method retrieves combined weather prediction error and the available power calculation error
	 * It was assumed that the smallest time interval unit is equal 10 min
	 *
	 * @param job job of interest
	 * @return entire power calculation error
	 */
	public double computeCombinedPowerError(final PowerJob job) {
		final Instant realJobStartTime = convertToRealTime(job.getStartTime());
		final Instant realJobEndTime = convertToRealTime(job.getEndTime());
		final double availablePowerError = computeIncorrectMaximumValProbability(realJobStartTime, realJobEndTime,
				INTERVAL_LENGTH_MIN);

		return min(1, availablePowerError + greenEnergyAgent.getWeatherPredictionError());
	}

	/**
	 * Computes available power available in the given moment
	 *
	 * @param time    time of the check
	 * @param weather monitoring data with com.greencloud.application.weather for requested timetable
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailablePower(final Instant time, final MonitoringData weather) {
		var availablePower = getPower(time, weather);
		var power = String.format("%.2f", availablePower);
		logger.info(CURRENT_AVAILABLE_POWER_LOG, greenEnergyAgent.getEnergyType(), power, time);

		return Optional.of(availablePower).filter(powerVal -> powerVal >= 0.0);
	}

	/**
	 * Method computes current green power in use for the green source
	 *
	 * @return current power in use
	 */
	public int getCurrentPowerInUseForGreenSource() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobStatusEnum.IN_PROGRESS)
						&& isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.mapToInt(job -> job.getKey().getPower())
				.sum();
	}

	/**
	 * Method updates the information on the green source GUI
	 */
	public void updateGreenSourceGUI() {
		final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();

		if (nonNull(greenEnergyAgentNode)) {
			greenEnergyAgentNode.updateMaximumCapacity(greenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity(),
					getCurrentPowerInUseForGreenSource());
			greenEnergyAgentNode.updateJobsCount(getJobCount());
			greenEnergyAgentNode.updateJobsOnHoldCount(getOnHoldJobCount());
			greenEnergyAgentNode.updateIsActive(getIsActiveState());
			greenEnergyAgentNode.updateTraffic(getCurrentPowerInUseForGreenSource());
		}
	}

	public AtomicInteger getStartedJobsInstances() {
		return startedJobsInstances;
	}

	public AtomicInteger getFinishedJobsInstances() {
		return finishedJobsInstances;
	}

	private synchronized Double getPower(Instant start, MonitoringData weather) {
		final double inUseCapacity = greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> ACCEPTED_JOB_STATUSES.contains(greenEnergyAgent.getPowerJobs().get(job)) &&
						job.isExecutedAtTime(start))
				.mapToInt(PowerJob::getPower)
				.sum();
		return greenEnergyAgent.manageGreenPower().getAvailablePower(weather, start) - inUseCapacity;
	}

	private int getOnHoldJobCount() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> JOB_ON_HOLD_STATUSES.contains(job.getValue())
						&& isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.toList()
				.size();
	}

	private int getJobCount() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> RUNNING_JOB_STATUSES.contains(job.getValue())
						&& isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.map(Map.Entry::getKey)
				.map(PowerJob::getJobId)
				.collect(Collectors.toSet())
				.size();
	}

	private boolean getIsActiveState() {
		return getCurrentPowerInUseForGreenSource() > 0 || getOnHoldJobCount() > 0;
	}
}
