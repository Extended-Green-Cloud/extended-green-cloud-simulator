package com.greencloud.application.agents.greenenergy.management;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INTERVAL_LENGTH_MIN;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.AVERAGE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.CURRENT_AVAILABLE_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_ACCEPTED_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_FAILED_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_START_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.utils.AlgorithmUtils.computeIncorrectMaximumValProbability;
import static com.greencloud.application.utils.AlgorithmUtils.getMinimalAvailablePowerDuringTimeStamp;
import static com.greencloud.application.utils.JobUtils.calculateExpectedJobEndTime;
import static com.greencloud.application.utils.JobUtils.getJobSuccessRatio;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.JOB_ON_HOLD_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.RUNNING_JOB_STATUSES;
import static com.greencloud.commons.job.JobResultType.ACCEPTED;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static com.greencloud.commons.job.JobResultType.FINISH;
import static com.greencloud.commons.job.JobResultType.STARTED;
import static java.lang.Math.min;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.ServerJob;
import com.gui.agents.GreenEnergyAgentNode;

/**
 * Set of methods used to manage the internal state of the green energy agent
 */
public class GreenEnergyStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyStateManagement.class);

	private final ConcurrentMap<JobResultType, Long> jobCounters;
	private final GreenEnergyAgent greenEnergyAgent;
	private final AtomicInteger weatherShortagesCounter;

	/**
	 * Default constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyStateManagement(GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
		this.weatherShortagesCounter = new AtomicInteger(0);
		this.jobCounters = Arrays.stream(JobResultType.values())
				.collect(Collectors.toConcurrentMap(status -> status, status -> 0L));
	}

	/**
	 * Method increments the counter of green source jobs
	 *
	 * @param jobInstanceId job identifier
	 * @param type          type of counter to increment
	 */
	public void incrementJobCounter(final JobInstanceIdentifier jobInstanceId, final JobResultType type) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		jobCounters.computeIfPresent(type, (key, val) -> val += 1);

		switch (type) {
			case FAILED -> logger.info(POWER_JOB_FAILED_LOG, jobCounters.get(FAILED));
			case ACCEPTED -> logger.info(POWER_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED));
			case STARTED -> logger.info(POWER_JOB_START_LOG, jobInstanceId, jobCounters.get(STARTED),
					jobCounters.get(ACCEPTED));
			case FINISH ->
					logger.info(POWER_JOB_FINISH_LOG, jobInstanceId, jobCounters.get(FINISH), jobCounters.get(STARTED));
		}
		updateGreenSourceGUI();
	}

	public AtomicInteger getWeatherShortagesCounter() {
		return weatherShortagesCounter;
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
	 * Method creates new instances for given server job which will be affected by the power shortage.
	 * If the power shortage will begin after the start of job execution -> job will be divided into 2
	 *
	 * Example:
	 * Job1 (start: 08:00, finish: 10:00)
	 * Power shortage start: 09:00
	 *
	 * Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage
	 * Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage
	 *
	 * @param serverJob          affected server job
	 * @param powerShortageStart time when power shortage starts
	 */
	public ServerJob divideServerJobForPowerShortage(final ServerJob serverJob, final Instant powerShortageStart) {
		if (powerShortageStart.isAfter(serverJob.getStartTime())) {
			final ServerJob affectedServerJobInstance = JobMapper.mapToJobNewStartTime(serverJob, powerShortageStart);
			final ServerJob notAffectedServerJobInstance = JobMapper.mapToJobNewEndTime(serverJob, powerShortageStart);
			final ExecutionJobStatusEnum currentJobStatus = greenEnergyAgent.getServerJobs().get(serverJob);

			greenEnergyAgent.getServerJobs().remove(serverJob);
			greenEnergyAgent.getServerJobs().put(affectedServerJobInstance, ExecutionJobStatusEnum.ON_HOLD_TRANSFER);
			greenEnergyAgent.getServerJobs().put(notAffectedServerJobInstance, currentJobStatus);
			greenEnergyAgent.addBehaviour(new HandleManualPowerSupplyFinish(greenEnergyAgent,
					calculateExpectedJobEndTime(affectedServerJobInstance),
					mapToJobInstanceId(affectedServerJobInstance)));
			updateGreenSourceGUI();
			return affectedServerJobInstance;
		} else {
			greenEnergyAgent.getServerJobs().replace(serverJob, ExecutionJobStatusEnum.ON_HOLD_TRANSFER);
			updateGreenSourceGUI();
			return serverJob;
		}
	}

	/**
	 * Computes power available during computation of the job being processed
	 *
	 * @param serverJob job of interest
	 * @param weather   monitoring data with com.greencloud.application.weather for requested timetable
	 * @param isNewJob  flag indicating whether job of interest is a processed new job
	 * @return available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailablePowerForJob(final ServerJob serverJob,
			final MonitoringData weather, final boolean isNewJob) {
		final Set<ExecutionJobStatusEnum> jobStatuses = isNewJob ? ACCEPTED_JOB_STATUSES : ACTIVE_JOB_STATUSES;
		final Set<ServerJob> serverJobsOfInterest = greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> jobStatuses.contains(job.getValue()))
				.map(Map.Entry::getKey)
				.map(JobMapper::mapToServerJobRealTime)
				.collect(Collectors.toSet());
		final Instant realJobStartTime = convertToRealTime(serverJob.getStartTime());
		final Instant realJobEndTime = convertToRealTime(serverJob.getEndTime());

		final double availablePower =
				getMinimalAvailablePowerDuringTimeStamp(
						serverJobsOfInterest,
						realJobStartTime,
						realJobEndTime,
						INTERVAL_LENGTH_MIN,
						greenEnergyAgent.manageGreenPower(),
						weather);
		final String power = String.format("%.2f", availablePower);

		MDC.put(MDC_JOB_ID, serverJob.getJobId());
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
	public double computeCombinedPowerError(final ServerJob job) {
		final Instant realJobStartTime = convertToRealTime(job.getStartTime());
		final Instant realJobEndTime = convertToRealTime(job.getEndTime());
		final double availablePowerError = computeIncorrectMaximumValProbability(realJobStartTime, realJobEndTime,
				INTERVAL_LENGTH_MIN);

		return min(1, availablePowerError + greenEnergyAgent.getWeatherPredictionError());
	}

	/**
	 * Computes available power available in the given moment
	 *
	 * @param time    time of the check (in real time)
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
		return greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(ExecutionJobStatusEnum.IN_PROGRESS)
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
			final double successRatio = getJobSuccessRatio(jobCounters.get(ACCEPTED), jobCounters.get(FAILED));

			greenEnergyAgentNode.updateMaximumCapacity(greenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity(),
					getCurrentPowerInUseForGreenSource());
			greenEnergyAgentNode.updateJobsCount(getJobCount());
			greenEnergyAgentNode.updateJobsOnHoldCount(getOnHoldJobCount());
			greenEnergyAgentNode.updateIsActive(getIsActiveState());
			greenEnergyAgentNode.updateTraffic(getCurrentPowerInUseForGreenSource());
			greenEnergyAgentNode.updateCurrentJobSuccessRatio(successRatio);
			writeStateToDatabase();
		}
	}

	public ConcurrentMap<JobResultType, Long> getJobCounters() {
		return jobCounters;
	}

	private void writeStateToDatabase() {
		final int currentMaxCapacity = greenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity();
		final double trafficOverall =
				currentMaxCapacity == 0 ? 0 : ((double) getCurrentPowerInUseForGreenSource()) / currentMaxCapacity;

		final GreenSourceMonitoringData greenSourceMonitoring = ImmutableGreenSourceMonitoringData.builder()
				.currentMaximumCapacity(currentMaxCapacity)
				.currentTraffic(trafficOverall)
				.weatherPredictionError(greenEnergyAgent.getWeatherPredictionError())
				.successRatio(getJobSuccessRatio(jobCounters.get(ACCEPTED), jobCounters.get(FAILED)))
				.build();
		greenEnergyAgent.writeMonitoringData(GREEN_SOURCE_MONITORING, greenSourceMonitoring);
	}

	private synchronized Double getPower(Instant start, MonitoringData weather) {
		final double inUseCapacity = greenEnergyAgent.getServerJobs().keySet().stream()
				.filter(job -> ACCEPTED_JOB_STATUSES.contains(greenEnergyAgent.getServerJobs().get(job)) &&
						isWithinTimeStamp(job, start))
				.mapToInt(ServerJob::getPower)
				.sum();
		return greenEnergyAgent.manageGreenPower().getAvailablePower(weather, start) - inUseCapacity;
	}

	private int getOnHoldJobCount() {
		return greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> JOB_ON_HOLD_STATUSES.contains(job.getValue())
						&& isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.toList()
				.size();
	}

	private int getJobCount() {
		return greenEnergyAgent.getServerJobs().entrySet().stream()
				.filter(job -> RUNNING_JOB_STATUSES.contains(job.getValue())
						&& isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.map(Map.Entry::getKey)
				.map(ServerJob::getJobId)
				.collect(Collectors.toSet())
				.size();
	}

	private boolean getIsActiveState() {
		return getCurrentPowerInUseForGreenSource() > 0 || getOnHoldJobCount() > 0;
	}
}
