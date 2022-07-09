package agents.server.domain;

import static common.GUIUtils.displayMessageArrow;
import static common.TimeUtils.getCurrentTime;
import static common.TimeUtils.isWithinTimeStamp;
import static domain.job.JobStatusEnum.*;
import static java.time.temporal.ChronoUnit.HOURS;
import static messages.domain.JobStatusMessageFactory.prepareFinishMessage;

import agents.server.ServerAgent;
import agents.server.behaviour.FinishJobExecution;
import agents.server.behaviour.StartJobExecution;
import com.gui.domain.nodes.ServerAgentNode;
import common.TimeUtils;
import common.mapper.JobMapper;
import domain.GreenSourceData;
import domain.job.ImmutableJob;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Set of utilities used to manage the internal state of the server agent */
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
   * Method computes the available power for given time frame
   *
   * @param startDate starting date
   * @param endDate end date
   * @return available power
   */
  public int getAvailableCapacity(final OffsetDateTime startDate, final OffsetDateTime endDate) {
    return serverAgent.getMaximumCapacity() - serverAgent.getServerJobs().entrySet().stream()
        .filter(
            job -> (TimeUtils.isWithinTimeStamp(startDate, endDate, job.getKey().getStartTime())
                || TimeUtils.isWithinTimeStamp(startDate, endDate, job.getKey().getEndTime())))
        .mapToInt(job -> job.getKey().getPower())
        .sum();
  }

  /**
   * Method performs default behaviour when the job is finished
   *
   * @param jobToFinish job to be finished
   * @param informCNA flag indicating whether cloud network should be informed about the job finish
   */
  public void finishJobExecution(final Job jobToFinish, final boolean informCNA) {
    final List<AID> receivers =
        informCNA
            ? List.of(
                serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()),
                serverAgent.getOwnerCloudNetworkAgent())
            : Collections.singletonList(
                serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()));
    final ACLMessage finishJobMessage =
        prepareFinishMessage(jobToFinish.getJobId(), jobToFinish.getStartTime(), receivers);
    serverAgent.getServerJobs().remove(jobToFinish);
    if (Objects.isNull(serverAgent.manage().getJobById(jobToFinish.getJobId()))) {
      serverAgent.getGreenSourceForJobMap().remove(jobToFinish.getJobId());
    }
    incrementFinishedJobs(jobToFinish.getJobId());
    displayMessageArrow(serverAgent, receivers);
    serverAgent.send(finishJobMessage);
  }

  /**
   * Method calculates the price for executing the job by given green source and server
   *
   * @param greenSourceData green source executing the job
   * @return full price
   */
  public double calculateServicePrice(final GreenSourceData greenSourceData) {
    var job = getJobById(greenSourceData.getJobId());
    var powerCost = job.getPower() * greenSourceData.getPricePerPowerUnit();
    var computingCost =
        HOURS.between(job.getEndTime(), job.getStartTime()) * serverAgent.getPricePerHour();
    return powerCost + computingCost;
  }

  /**
   * Method returns the instance of the job for current time
   *
   * @param jobId unique job identifier
   * @return pair of job and current status
   */
  public Map.Entry<Job, JobStatusEnum> getCurrentJobInstance(final String jobId) {
    final OffsetDateTime currentTime = getCurrentTime();
    return serverAgent.getServerJobs().entrySet().stream()
        .filter(
            jobEntry -> {
              final Job job = jobEntry.getKey();
              return job.getJobId().equals(jobId)
                  && ((job.getStartTime().isBefore(currentTime)
                          && job.getEndTime().isAfter(currentTime))
                      || job.getEndTime().equals(currentTime));
            })
        .findFirst()
        .orElse(null);
  }

  /**
   * Method retrieves the job by the job id and star time from job map
   *
   * @param jobId job identifier
   * @param startTime job start time
   * @return job
   */
  public Job getJobByIdAndStartDate(final String jobId, final OffsetDateTime startTime) {
    return serverAgent.getServerJobs().keySet().stream()
        .filter(job -> job.getJobId().equals(jobId) && job.getStartTime().isEqual(startTime))
        .findFirst()
        .orElse(null);
  }

  /**
   * Method retrieves the job by the job instance id
   *
   * @param jobInstanceId job instance identifier
   * @return job
   */
  public Job getJobByIdAndStartDate(final JobInstanceIdentifier jobInstanceId) {
    return serverAgent.getServerJobs().keySet().stream()
        .filter(
            job ->
                job.getJobId().equals(jobInstanceId.getJobId())
                    && job.getStartTime().isEqual(jobInstanceId.getStartTime()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Method retrieves the job by the job id and end time from job map
   *
   * @param jobId job identifier
   * @param endTime job end time
   * @return job
   */
  public Job getJobByIdAndEndDate(final String jobId, final OffsetDateTime endTime) {
    return serverAgent.getServerJobs().keySet().stream()
        .filter(job -> job.getJobId().equals(jobId) && job.getEndTime().isEqual(endTime))
        .findFirst()
        .orElse(null);
  }

  /**
   * Method retrieves the job based on the given id
   *
   * @param jobId unique job identifier
   * @return Job
   */
  public Job getJobById(final String jobId) {
    return serverAgent.getServerJobs().keySet().stream()
        .filter(job -> job.getJobId().equals(jobId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Method verifies if there is only 1 instance of the given job
   *
   * @param jobId unique job identifier
   * @return boolean
   */
  public boolean isJobUnique(final String jobId) {
    return serverAgent.getServerJobs().keySet().stream()
            .filter(job -> job.getJobId().equals(jobId))
            .toList()
            .size()
        == 1;
  }

  /**
   * Method increments the count of started jobs
   *
   * @param jobId unique job identifier
   */
  public void incrementStartedJobs(final String jobId) {
    if (isJobUnique(jobId)) {
      uniqueStartedJobs.getAndAdd(1);
      logger.info(
          "[{}] Started job {}. Number of unique started jobs is {}",
          serverAgent.getLocalName(),
          jobId,
          uniqueStartedJobs);
    }
    startedJobsInstances.getAndAdd(1);
    logger.info(
        "[{}] Started job instance {}. Number of started job instances is {}",
        serverAgent.getLocalName(),
        jobId,
        startedJobsInstances);
    updateServerGUI();
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
          serverAgent.getLocalName(),
          jobId,
          uniqueFinishedJobs,
          uniqueStartedJobs);
    }
    finishedJobsInstances.getAndAdd(1);
    logger.info(
        "[{}] Finished job instance {}. Number of finished job instances is {} out of {} started",
        serverAgent.getLocalName(),
        jobId,
        finishedJobsInstances,
        startedJobsInstances);
    updateServerGUI();
  }

  /**
   * Method changes the server's maximum capacity
   *
   * @param newMaximumCapacity new maximum capacity value
   */
  public void updateMaximumCapacity(final int newMaximumCapacity) {
    serverAgent.setMaximumCapacity(newMaximumCapacity);
    ((ServerAgentNode) serverAgent.getAgentNode())
        .updateMaximumCapacity(serverAgent.getMaximumCapacity());
  }

  /**
   * Method creates new instances for given job which will be affected by the power shortage
   *
   * @param job affected job
   * @param powerShortageStart time when power shortage starts
   */
  public void divideJobForPowerShortage(final Job job, final OffsetDateTime powerShortageStart) {
    if (powerShortageStart.isAfter(job.getStartTime())
        && !powerShortageStart.equals(job.getStartTime())) {
      final Job onBackupEnergyInstance =
          ImmutableJob.builder()
              .jobId(job.getJobId())
              .clientIdentifier(job.getClientIdentifier())
              .power(job.getPower())
              .startTime(powerShortageStart)
              .endTime(job.getEndTime())
              .build();
      final Job finishedPowerJobInstance =
          ImmutableJob.builder()
              .jobId(job.getJobId())
              .clientIdentifier(job.getClientIdentifier())
              .power(job.getPower())
              .startTime(job.getStartTime())
              .endTime(powerShortageStart)
              .build();
      final JobStatusEnum currentJobStatus = serverAgent.getServerJobs().get(job);
      serverAgent.getServerJobs().remove(job);
      serverAgent
          .getServerJobs()
          .put(onBackupEnergyInstance, JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_TEMPORARY);
      serverAgent.getServerJobs().put(finishedPowerJobInstance, currentJobStatus);
      serverAgent.addBehaviour(
          StartJobExecution.createFor(
              serverAgent, JobMapper.mapToJobInstanceId(onBackupEnergyInstance), false, true));
      if (getCurrentTime().isBefore(finishedPowerJobInstance.getStartTime())) {
        serverAgent.addBehaviour(
            StartJobExecution.createFor(
                serverAgent, JobMapper.mapToJobInstanceId(finishedPowerJobInstance), true, false));
      } else {
        serverAgent.addBehaviour(
            FinishJobExecution.createFor(serverAgent, finishedPowerJobInstance, false));
      }
    } else {
      serverAgent.getServerJobs().replace(job, JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_TEMPORARY);
      updateServerGUI();
    }
  }

  private List<Job> getUniqueJobsForTimeStamp(
      final OffsetDateTime startDate, final OffsetDateTime endDate) {
    return serverAgent.getServerJobs().keySet().stream()
        .filter(job -> !serverAgent.getServerJobs().get(job).equals(PROCESSING))
        .filter(
            job ->
                TimeUtils.isWithinTimeStamp(startDate, endDate, job.getStartTime())
                    || TimeUtils.isWithinTimeStamp(startDate, endDate, job.getEndTime()))
        .map(Job::getJobId)
        .collect(Collectors.toSet())
        .stream()
        .collect(Collectors.toMap(jobId -> jobId, this::getJobById))
        .values()
        .stream()
        .toList();
  }

  /** Method updates the information on the server GUI */
  public void updateServerGUI() {
    final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();
    serverAgentNode.updateMaximumCapacity(serverAgent.getMaximumCapacity());
    serverAgentNode.updateJobsCount(getJobCount());
    serverAgentNode.updateClientNumber(getClientNumber());
    serverAgentNode.updateIsActive(getIsActiveState(), getIsActiveBackUpState());
    serverAgentNode.updateTraffic(getCurrentPowerInUseForServer());
    serverAgentNode.updateBackUpTraffic(getCurrentBackUpPowerInUseForServer());
  }

  /** Method updates the client number */
  public void updateClientNumber() {
    ((ServerAgentNode) serverAgent.getAgentNode()).updateClientNumber(getClientNumber());
  }

  private int getJobCount() {
    return serverAgent.getServerJobs().entrySet().stream()
        .filter(
            job ->
                JOB_IN_PROGRESS.contains(job.getValue())
                    && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
        .map(Map.Entry::getKey)
        .map(Job::getJobId)
        .collect(Collectors.toSet())
        .size();
  }

  private int getClientNumber() {
    return serverAgent.getGreenSourceForJobMap().size();
  }

  private int getCurrentPowerInUseForServer() {
    return serverAgent.getServerJobs().entrySet().stream()
        .filter(
            job ->
                job.getValue().equals(JobStatusEnum.IN_PROGRESS)
                    && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
        .mapToInt(job -> job.getKey().getPower())
        .sum();
  }

  private int getCurrentBackUpPowerInUseForServer() {
    return serverAgent.getServerJobs().entrySet().stream()
        .filter(
            job ->
                JOB_ON_BACK_UP.contains(job.getValue())
                    && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
        .mapToInt(job -> job.getKey().getPower())
        .sum();
  }

  private boolean getIsActiveState() {
    return getCurrentPowerInUseForServer() > 0 || getCurrentBackUpPowerInUseForServer() > 0;
  }

  private boolean getIsActiveBackUpState() {
    return getCurrentBackUpPowerInUseForServer() > 0;
  }
}
