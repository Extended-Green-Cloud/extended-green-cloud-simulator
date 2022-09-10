package agents.greenenergy.domain;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static domain.job.JobStatusEnum.ACTIVE_JOB_STATUSES;
import static domain.job.JobStatusEnum.JOB_ON_HOLD;
import static java.util.stream.Collectors.toMap;
import static mapper.JsonMapper.getMapper;
import static utils.GUIUtils.displayMessageArrow;
import static utils.JobMapUtils.getJobCount;
import static utils.TimeUtils.getCurrentTime;
import static utils.TimeUtils.isWithinTimeStamp;
import static utils.JobMapUtils.isJobUnique;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.agents.GreenEnergyAgentNode;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.FinishJobManually;
import common.mapper.JobMapper;
import domain.MonitoringData;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.lang.acl.ACLMessage;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Set of utilities used to manage the internal state of the green energy agent
 */
public class GreenEnergyStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyStateManagement.class);
	protected final AtomicInteger uniqueStartedJobs;
	protected final AtomicInteger uniqueFinishedJobs;
	protected final AtomicInteger startedJobsInstances;
	protected final AtomicInteger finishedJobsInstances;
	private final GreenEnergyAgent greenEnergyAgent;

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
		if (isJobUnique(greenEnergyAgent.getPowerJobs(), jobId)) {
			uniqueStartedJobs.getAndAdd(1);
			logger.info("[{}] Started job {}. Number of unique started jobs is {}", greenEnergyAgent.getLocalName(),
					jobId, uniqueStartedJobs);
		}
		startedJobsInstances.getAndAdd(1);
		logger.info("[{}] Started job instance {}. Number of started job instances is {}",
				greenEnergyAgent.getLocalName(), jobId, startedJobsInstances);
		updateGreenSourceGUI();
	}

	/**
	 * Method increments the count of finished jobs
	 *
	 * @param jobId unique identifier of the job
	 */
	public void incrementFinishedJobs(final String jobId) {
		if (isJobUnique(greenEnergyAgent.getPowerJobs(), jobId)) {
			uniqueFinishedJobs.getAndAdd(1);
			logger.info(
					"[{}] Finished job {}. Number of unique finished jobs is {} out of {} started",
					greenEnergyAgent.getLocalName(),
					jobId,
					uniqueFinishedJobs,
					uniqueStartedJobs);
		}
		finishedJobsInstances.getAndAdd(1);
		logger.info(
				"[{}] Finished job instance {}. Number of finished job instances is {} out of {} started",
				greenEnergyAgent.getLocalName(),
				jobId,
				finishedJobsInstances,
				startedJobsInstances);
		updateGreenSourceGUI();
	}

	/**
	 * Method changes the green source's maximum capacity
	 *
	 * @param newMaximumCapacity new maximum capacity value
	 */
	public void updateMaximumCapacity(final int newMaximumCapacity) {
		greenEnergyAgent.setMaximumCapacity(newMaximumCapacity);
		((GreenEnergyAgentNode) greenEnergyAgent.getAgentNode()).updateMaximumCapacity(
				greenEnergyAgent.getMaximumCapacity());
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
			final Date endDate = Date.from(
					affectedPowerJobInstance.getEndTime().plusMillis(MAX_ERROR_IN_JOB_FINISH));
			greenEnergyAgent.addBehaviour(
					new FinishJobManually(greenEnergyAgent, endDate,
							JobMapper.mapToJobInstanceId(affectedPowerJobInstance)));
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
						Stream.of(candidateJob.getStartTime(), candidateJob.getEndTime()),
						Stream.concat(
								validJobs.stream().map(PowerJob::getStartTime),
								validJobs.stream().map(PowerJob::getEndTime)))
				.distinct()
				.toList();
	}

	/**
	 * Computes average power available during computation of the job being processed
	 *
	 * @param powerJob job being processed (not yet accepted!)
	 * @param weather  monitoring data with weather for requested timetable
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAverageAvailablePower(
			final PowerJob powerJob, final MonitoringData weather) {
		var powerChart = getPowerChart(powerJob, weather, true);
		var availablePower = powerChart.values().stream().mapToDouble(a -> a).average().orElse(0.0D);
		logger.info(
				"[{}] Calculated available {} average power {} between {} and {}",
				greenEnergyAgent.getName(),
				greenEnergyAgent.getEnergyType(),
				String.format("%.2f", availablePower),
				powerJob.getStartTime(),
				powerJob.getEndTime());
		if (powerChart.values().stream().anyMatch(value -> value <= 0)) {
			return Optional.empty();
		}
		return Optional.of(availablePower);
	}

	/**
	 * Computes average power available during computation of the job being checked before putting on hold job to in progress
	 *
	 * @param powerJob job being processed (which is on hold)
	 * @param weather  monitoring data with weather for requested timetable
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAverageAvailablePowerCheckForJobOnHold(final PowerJob powerJob,
			final MonitoringData weather) {
		var powerChart = getPowerChart(powerJob, weather, false);
		var availablePower = powerChart.values().stream().mapToDouble(a -> a).average().getAsDouble();
		logger.info("[{}] Calculated available {} average power {} between {} and {}", greenEnergyAgent.getName(),
				greenEnergyAgent.getEnergyType(),
				String.format("%.2f", availablePower), powerJob.getStartTime(), powerJob.getEndTime());
		return Optional.of(availablePower);
	}

	/**
	 * Method updates the information on the green source GUI
	 */
	public void updateGreenSourceGUI() {
		final GreenEnergyAgentNode serverAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();
		serverAgentNode.updateMaximumCapacity(greenEnergyAgent.getMaximumCapacity());
		serverAgentNode.updateJobsCount(getJobCount(greenEnergyAgent.getPowerJobs()));
		serverAgentNode.updateJobsOnHoldCount(getOnHoldJobCount());
		serverAgentNode.updateIsActive(getIsActiveState());
		serverAgentNode.updateTraffic(getCurrentPowerInUseForGreenSource());
	}

	public MonitoringData readMonitoringData(ACLMessage message, ACLMessage originalMessage) {
		try {
			return getMapper().readValue(message.getContent(), MonitoringData.class);
		} catch (JsonProcessingException e) {
			logger.info("[{}] I didn't understand the response with the weather data, sending refuse message to server",
					greenEnergyAgent.getName());
			greenEnergyAgent.send(ReplyMessageFactory.prepareRefuseReply(originalMessage.createReply()));
		}
		return null;
	}

	/**
	 * Computes available power available in the given moment
	 *
	 * @param time    time of the check
	 * @param weather monitoring data with weather for requested timetable
	 * @return average available power as decimal or empty optional if power not available
	 */
	public synchronized Optional<Double> getAvailablePower(final Instant time, final MonitoringData weather) {
		var availablePower = getPower(time, weather);
		logger.info("[{}] Calculated available {} power {} at {}", greenEnergyAgent.getName(),
				greenEnergyAgent.getEnergyType(),
				String.format("%.2f", availablePower), time);
		return Optional.of(availablePower).filter(power -> power >= 0.0);
	}

	private synchronized Double getPower(Instant start, MonitoringData weather) {
		var powerJobs = greenEnergyAgent.getPowerJobs().keySet().stream()
				.filter(job -> ACCEPTED_JOB_STATUSES.contains(greenEnergyAgent.getPowerJobs().get(job)))
				.toList();

		if (powerJobs.isEmpty()) {
			return greenEnergyAgent.getCapacity(weather, start);
		}

		return powerJobs.stream()
				.filter(job -> job.isExecutedAtTime(start))
				.map(PowerJob::getPower)
				.map(power -> greenEnergyAgent.getCapacity(weather, start) - power)
				.mapToDouble(Double::doubleValue)
				.average()
				.orElseGet(() -> 0.0);
	}

	private synchronized Map<Instant, Double> getPowerChart(PowerJob powerJob, final MonitoringData weather,
			final boolean isNewJob) {
		var start = powerJob.getStartTime();
		var end = powerJob.getEndTime();
		var timetable = getJobsTimetable(powerJob).stream()
				.filter(time -> isWithinTimeStamp(start, end, time))
				.toList();
		var powerJobs = isNewJob ?
				greenEnergyAgent.getPowerJobs().keySet().stream()
						.filter(job -> ACCEPTED_JOB_STATUSES.contains(greenEnergyAgent.getPowerJobs().get(job)))
						.toList() :
				greenEnergyAgent.getPowerJobs().keySet().stream()
						.filter(job -> ACTIVE_JOB_STATUSES.contains(greenEnergyAgent.getPowerJobs().get(job)))
						.toList();

		if (powerJobs.isEmpty()) {
			return timetable.stream()
					.collect(toMap(Function.identity(), time -> greenEnergyAgent.getCapacity(weather, time)));
		}

		return timetable.stream()
				.collect(toMap(Function.identity(), time ->
						powerJobs.stream()
								.filter(job -> job.isExecutedAtTime(time))
								.map(PowerJob::getPower)
								.map(power -> greenEnergyAgent.getCapacity(weather, time) - power)
								.mapToDouble(a -> a)
								.average()
								.orElseGet(() -> 0.0)));
	}

	public int getCurrentPowerInUseForGreenSource() {
		return greenEnergyAgent.getPowerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobStatusEnum.IN_PROGRESS)
						&& isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.mapToInt(job -> job.getKey().getPower())
				.sum();
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
		return getCurrentPowerInUseForGreenSource() > 0 || getOnHoldJobCount() > 0;
	}
}
