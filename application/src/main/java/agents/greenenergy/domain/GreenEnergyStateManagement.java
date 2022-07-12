package agents.greenenergy.domain;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.MAX_ERROR_IN_JOB_FINISH;
import static common.GUIUtils.displayMessageArrow;
import static common.TimeUtils.getCurrentTime;
import static common.TimeUtils.isWithinTimeStamp;
import static domain.job.JobStatusEnum.JOB_IN_PROGRESS;
import static domain.job.JobStatusEnum.JOB_ON_HOLD;
import static java.util.stream.Collectors.toMap;
import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.FinishJobManually;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.domain.nodes.GreenEnergyAgentNode;
import common.mapper.JobMapper;
import domain.MonitoringData;
import domain.job.ImmutablePowerJob;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.lang.acl.ACLMessage;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Method retrieves the job by the job id from job map
     *
     * @param jobId job identifier
     * @return job
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
    public PowerJob getJobByIdAndStartDate(final String jobId, final OffsetDateTime startTime) {
        return greenEnergyAgent.getPowerJobs().keySet().stream()
                .filter(job -> job.getJobId().equals(jobId) && job.getStartTime().isEqual(startTime))
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
                        && job.getStartTime().isEqual(jobInstanceId.getStartTime()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Method retrieves the job by the job id and end time from job map
     *
     * @param jobId   job identifier
     * @param endTime job end time
     * @return job
     */
    public PowerJob getJobByIdAndEndDate(final String jobId, final OffsetDateTime endTime) {
        return greenEnergyAgent.getPowerJobs().keySet().stream()
                .filter(job -> job.getJobId().equals(jobId) && job.getEndTime().isEqual(endTime))
                .findFirst()
                .orElse(null);
    }

    /**
     * Method increments the count of started jobs
     *
     * @param jobId unique job identifier
     */
    public void incrementStartedJobs(final String jobId) {
        if (isJobUnique(jobId)) {
            uniqueStartedJobs.getAndAdd(1);
            logger.info("[{}] Started job {}. Number of unique started jobs is {}", greenEnergyAgent.getLocalName(), jobId, uniqueStartedJobs);
        }
        startedJobsInstances.getAndAdd(1);
        logger.info("[{}] Started job instance {}. Number of started job instances is {}", greenEnergyAgent.getLocalName(), jobId, startedJobsInstances);
        updateGreenSourceGUI();
    }

    /**
     * Method increments the count of finished jobs
     *
     * @param jobId unique identifier of the job
     */
    public void incrementFinishedJobs(final String jobId) {
        if (isJobUnique(jobId)) {
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
        ((GreenEnergyAgentNode) greenEnergyAgent.getAgentNode()).updateMaximumCapacity(greenEnergyAgent.getMaximumCapacity());
    }

    /**
     * Method creates new instances for given power job which will be affected by the power shortage
     *
     * @param powerJob           affected job
     * @param powerShortageStart time when power shortage starts
     */
    public PowerJob divideJobForPowerShortage(
            final PowerJob powerJob, final OffsetDateTime powerShortageStart) {
        if (powerShortageStart.isAfter(powerJob.getStartTime())) {
            final PowerJob onHoldJobInstance =
                    ImmutablePowerJob.builder()
                            .jobId(powerJob.getJobId())
                            .power(powerJob.getPower())
                            .startTime(powerShortageStart)
                            .endTime(powerJob.getEndTime())
                            .build();
            final PowerJob finishedPowerJob =
                    ImmutablePowerJob.builder()
                            .jobId(powerJob.getJobId())
                            .power(powerJob.getPower())
                            .startTime(powerJob.getStartTime())
                            .endTime(powerShortageStart)
                            .build();
            final JobStatusEnum currentJobStatus = greenEnergyAgent.getPowerJobs().get(powerJob);
            greenEnergyAgent.getPowerJobs().remove(powerJob);
            greenEnergyAgent.getPowerJobs().put(onHoldJobInstance, JobStatusEnum.ON_HOLD_TEMPORARY);
            greenEnergyAgent.getPowerJobs().put(finishedPowerJob, currentJobStatus);
            final Date endDate =
                    Date.from(
                            onHoldJobInstance
                                    .getEndTime()
                                    .plus(MAX_ERROR_IN_JOB_FINISH, ChronoUnit.MILLIS)
                                    .toInstant());
            greenEnergyAgent.addBehaviour(
                    new FinishJobManually(
                            greenEnergyAgent, endDate, JobMapper.mapToJobInstanceId(onHoldJobInstance)));
            updateGreenSourceGUI();
            return onHoldJobInstance;
        } else {
            greenEnergyAgent.getPowerJobs().replace(powerJob, JobStatusEnum.ON_HOLD_TEMPORARY);
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
            .filter(entry -> !JOB_IN_PROGRESS.contains(entry.getValue()))
            .map(Entry::getKey)
            .toList();
        return Stream.concat(
                Stream.of(candidateJob.getStartTime(), candidateJob.getEndTime()),
                Stream.concat(
                    validJobs.stream().map(PowerJob::getStartTime),
                    validJobs.stream().map(PowerJob::getEndTime)))
            .map(OffsetDateTime::toInstant)
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
        var powerChart = getPowerChart(powerJob, weather);
        var availablePower = powerChart.values().stream().mapToDouble(a -> a).average().getAsDouble();
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
     * Computes average power available during computation of the job being checked before execution
     *
     * @param powerJob job being processed (not yet accepted!)
     * @param weather  monitoring data with weather for requested timetable
     * @return average available power as decimal or empty optional if power not available
     */
    public synchronized Optional<Double> getAverageAvailablePowerCheck(final PowerJob powerJob,
                                                                       final MonitoringData weather) {
        var powerChart = getPowerChart(powerJob, weather);
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
        serverAgentNode.updateJobsCount(getJobCount());
        serverAgentNode.updateIsActive(getIsActiveState(), getHasJobsOnHold());
        serverAgentNode.updateTraffic(getCurrentPowerInUseForGreenSource());
        serverAgentNode.updateJobsOnHold(getOnHoldJobCount());
    }

    public void handleRefuse(final ACLMessage message, final PowerJob powerJob) {
        logger.info("[{}] Weather data not available, sending refuse message to server.", greenEnergyAgent.getName());
        greenEnergyAgent.getPowerJobs().remove(powerJob);
        displayMessageArrow(greenEnergyAgent, message.getAllReceiver());
        greenEnergyAgent.send(ReplyMessageFactory.prepareRefuseReply(message.createReply()));
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
  public synchronized Optional<Double> getAvailablePower(final OffsetDateTime time, final MonitoringData weather) {
    var availablePower = getPower(time.toInstant(), weather);
    logger.info(
        "[{}] Calculated available {} power {} at {}",
        greenEnergyAgent.getName(),
        greenEnergyAgent.getEnergyType(),
        String.format("%.2f", availablePower),
        time);
    return Optional.of(availablePower).filter(power -> power >= 0.0);
  }


  private synchronized Double getPower(Instant start, MonitoringData weather) {
    var powerJobs = greenEnergyAgent.getPowerJobs().keySet().stream()
        .filter(job -> JOB_IN_PROGRESS.contains(greenEnergyAgent.getPowerJobs().get(job)))
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

    private boolean isJobUnique(final String jobId) {
        return greenEnergyAgent.getPowerJobs().keySet().stream()
                .filter(job -> job.getJobId().equals(jobId))
                .toList()
                .size()
                == 1;
    }

    private synchronized Map<Instant, Double> getPowerChart(PowerJob powerJob, final MonitoringData weather) {
        var start = powerJob.getStartTime().toInstant();
        var end = powerJob.getEndTime().toInstant();
        var timetable = getJobsTimetable(powerJob).stream()
                .filter(time -> isWithinTimeStamp(start, end, time))
                .toList();
        var powerJobs = greenEnergyAgent.getPowerJobs().keySet().stream()
                .filter(job -> JOB_IN_PROGRESS.contains(greenEnergyAgent.getPowerJobs().get(job)))
                .toList();

        if (powerJobs.isEmpty()) {
            return timetable.stream().collect(toMap(Function.identity(), time -> greenEnergyAgent.getCapacity(weather, time)));
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

    private int getCurrentPowerInUseForGreenSource() {
        return greenEnergyAgent.getPowerJobs().entrySet().stream()
                .filter(job -> job.getValue().equals(JobStatusEnum.IN_PROGRESS)
                        && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
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

    private int getJobCount() {
        return greenEnergyAgent.getPowerJobs().entrySet().stream()
                .filter(job -> JOB_IN_PROGRESS.contains(job.getValue())
                        && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
                .map(Map.Entry::getKey)
                .map(PowerJob::getJobId)
                .collect(Collectors.toSet())
                .size();
    }

    private boolean getIsActiveState() {
        return getCurrentPowerInUseForGreenSource() > 0 || getHasJobsOnHold();
    }

    private boolean getHasJobsOnHold() {
        return getOnHoldJobCount() > 0;
    }
}
