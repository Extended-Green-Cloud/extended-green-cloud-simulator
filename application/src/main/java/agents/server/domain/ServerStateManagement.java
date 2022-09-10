package agents.server.domain;

import static agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static domain.job.JobStatusEnum.JOB_ON_HOLD;
import static domain.job.JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static messages.domain.factory.JobStatusMessageFactory.prepareFinishMessage;
import static utils.AlgorithmUtils.getMaximumUsedPowerDuringTimeStamp;
import static utils.GUIUtils.displayMessageArrow;
import static utils.TimeUtils.differenceInHours;
import static utils.TimeUtils.getCurrentTime;
import static utils.TimeUtils.isWithinTimeStamp;
import static utils.JobMapUtils.getJobById;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gui.agents.ServerAgentNode;

import agents.server.ServerAgent;
import agents.server.behaviour.jobexecution.handler.HandleJobFinish;
import agents.server.behaviour.jobexecution.handler.HandleJobStart;
import common.mapper.JobMapper;
import domain.GreenSourceData;
import domain.job.Job;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

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
		final Set<Job> jobsOfInterest = serverAgent.getServerJobs().keySet().stream()
				.filter(job -> Objects.isNull(jobToExclude) || !JobMapper.mapToJobInstanceId(job).equals(jobToExclude))
				.filter(job -> Objects.isNull(powerSourceType) ||
						powerSourceType.getJobStatuses().contains(serverAgent.getServerJobs().get(job)))
				.collect(Collectors.toSet());
		final int maxUsedPower =
				getMaximumUsedPowerDuringTimeStamp(jobsOfInterest, startDate, endDate);
		return serverAgent.getCurrentMaximumCapacity() - maxUsedPower;
	}

	/**
	 * Method performs job finishing action
	 *
	 * @param jobToFinish job to be finished
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 */
	public void finishJobExecution(final Job jobToFinish, final boolean informCNA) {
		final JobStatusEnum jobStatusEnum = serverAgent.getServerJobs().get(jobToFinish);

		sendFinishInformation(jobToFinish, informCNA);
		updateStateAfterJobFinish(jobToFinish);

		if (jobStatusEnum.equals(IN_PROGRESS_BACKUP_ENERGY)) {
			final Map<Job, JobStatusEnum> jobsWithinTimeStamp = serverAgent.getServerJobs().entrySet().stream()
					.filter(job -> isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(),
							getCurrentTime()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			supplyJobsWithBackupPower(jobsWithinTimeStamp);
		}
	}

	/**
	 * Method calculates the price for executing the job by given green source and server
	 *
	 * @param greenSourceData green source executing the job
	 * @return full price
	 */
	public double calculateServicePrice(final GreenSourceData greenSourceData) {
		var job = getJobById(serverAgent.getServerJobs(), greenSourceData.getJobId());
		var powerCost = job.getPower() * greenSourceData.getPricePerPowerUnit();
		var computingCost = differenceInHours(job.getStartTime(), job.getEndTime()) * serverAgent.getPricePerHour();
		return powerCost + computingCost;
	}

	/**
	 * Method returns the instance of the job for current time
	 *
	 * @param jobId unique job identifier
	 * @return pair of job and current status
	 */
	public Map.Entry<Job, JobStatusEnum> getCurrentJobInstance(final String jobId) {
		final Instant currentTime = getCurrentTime();
		return serverAgent.getServerJobs().entrySet().stream().filter(jobEntry -> {
			final Job job = jobEntry.getKey();
			return job.getJobId().equals(jobId) && (
					(job.getStartTime().isBefore(currentTime) && job.getEndTime().isAfter(currentTime))
							|| job.getEndTime().equals(currentTime));
		}).findFirst().orElse(null);
	}

	/**
	 * Method retrieves the job by the job id and star time from job map
	 *
	 * @param jobId     job identifier
	 * @param startTime job start time
	 * @return job
	 */
	public Job getJobByIdAndStartDate(final String jobId, final Instant startTime) {
		return serverAgent.getServerJobs().keySet().stream()
				.filter(job -> job.getJobId().equals(jobId) && job.getStartTime().equals(startTime)).findFirst()
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
				.filter(job -> job.getJobId().equals(jobInstanceId.getJobId()) && job.getStartTime()
						.equals(jobInstanceId.getStartTime())).findFirst().orElse(null);
	}

	/**
	 * Method verifies if there is only 1 instance of the given job
	 *
	 * @param jobId unique job identifier
	 * @return boolean
	 */
	public boolean isJobUnique(final String jobId) {
		return serverAgent.getServerJobs().keySet().stream().filter(job -> job.getJobId().equals(jobId)).toList().size()
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
			logger.info("[{}] Started job {}. Number of unique started jobs is {}", serverAgent.getLocalName(),
					jobId,
					uniqueStartedJobs);
		}
		startedJobsInstances.getAndAdd(1);
		logger.info("[{}] Started job instance {}. Number of started job instances is {}",
				serverAgent.getLocalName(),
				jobId, startedJobsInstances);
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
			logger.info("[{}] Finished job {}. Number of unique finished jobs is {} out of {} started",
					serverAgent.getLocalName(), jobId, uniqueFinishedJobs, uniqueStartedJobs);
		}
		finishedJobsInstances.getAndAdd(1);
		logger.info("[{}] Finished job instance {}. Number of finished job instances is {} out of {} started",
				serverAgent.getLocalName(), jobId, finishedJobsInstances, startedJobsInstances);
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
			serverAgentNode.updateMaximumCapacity(serverAgent.getCurrentMaximumCapacity());
		}
	}

	/**
	 * Method creates new instances for given job which will be affected by the power shortage.
	 * If the power shortage will begin after the start of job execution -> job will be divided into 2
	 *
	 * Example:
	 * Job1 (start: 08:00, finish: 10:00)
	 * Power shortage start: 09:00
	 *
	 * Job1Instance1: (start: 08:00, finish: 09:00) <- job not affected by power shortage
	 * Job1Instance2: (start: 09:00, finish: 10:00) <- job affected by power shortage
	 *
	 * @param job                affected job
	 * @param powerShortageStart time when power shortage starts
	 */
	public Job divideJobForPowerShortage(final Job job, final Instant powerShortageStart) {
		if (powerShortageStart.isAfter(job.getStartTime()) && !powerShortageStart.equals(job.getStartTime())) {
			final Job affectedJobInstance = JobMapper.mapToJobNewStartTime(job, powerShortageStart);
			final Job notAffectedJobInstance = JobMapper.mapToJobNewEndTime(job, powerShortageStart);
			final JobStatusEnum currentJobStatus = serverAgent.getServerJobs().get(job);

			serverAgent.getServerJobs().remove(job);
			serverAgent.getServerJobs().put(affectedJobInstance, JobStatusEnum.ON_HOLD_TRANSFER);
			serverAgent.getServerJobs().put(notAffectedJobInstance, currentJobStatus);

			serverAgent.addBehaviour(HandleJobStart.createFor(serverAgent, affectedJobInstance, false, true));
			if (getCurrentTime().isBefore(notAffectedJobInstance.getStartTime())) {
				serverAgent.addBehaviour(
						HandleJobStart.createFor(serverAgent, notAffectedJobInstance, true, false));
			} else if (getCurrentTime().isBefore(notAffectedJobInstance.getEndTime())) {
				serverAgent.addBehaviour(HandleJobFinish.createFor(serverAgent, notAffectedJobInstance, false));
			}

			return affectedJobInstance;
		} else {
			serverAgent.getServerJobs().replace(job, JobStatusEnum.ON_HOLD_TRANSFER);
			updateServerGUI();
			return job;
		}
	}

	/**
	 * Method updates the information on the server GUI
	 */
	public void updateServerGUI() {
		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();

		if (Objects.nonNull(serverAgentNode)) {
			serverAgentNode.updateMaximumCapacity(serverAgent.getCurrentMaximumCapacity());
			serverAgentNode.updateJobsCount(getJobCount());
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

	private void sendFinishInformation(final Job jobToFinish, final boolean informCNA) {
		final List<AID> receivers = informCNA ?
				List.of(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()),
						serverAgent.getOwnerCloudNetworkAgent()) :
				Collections.singletonList(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()));
		final ACLMessage finishJobMessage = prepareFinishMessage(jobToFinish.getJobId(), jobToFinish.getStartTime(),
				receivers);

		displayMessageArrow(serverAgent, receivers);
		serverAgent.send(finishJobMessage);
	}

	private void updateStateAfterJobFinish(final Job jobToFinish) {
		incrementFinishedJobs(jobToFinish.getJobId());
		if (isJobUnique(jobToFinish.getJobId())) {
			serverAgent.getGreenSourceForJobMap().remove(jobToFinish.getJobId());
		}
		serverAgent.getServerJobs().remove(jobToFinish);
	}

	private void supplyJobsWithBackupPower(final Map<Job, JobStatusEnum> jobEntries) {
		jobEntries.entrySet().stream()
				.filter(job -> job.getValue().equals(ON_HOLD_SOURCE_SHORTAGE))
				.forEach(jobEntry -> {
					final Job job = jobEntry.getKey();
					if (getAvailableCapacity(job.getStartTime(), job.getEndTime(), JobMapper.mapToJobInstanceId(job),
							BACK_UP_POWER) >= job.getPower()) {
						logger.info("[{}] Supplying job {} with back up power", serverAgent.getName(), job.getJobId());
						serverAgent.getServerJobs().replace(job, IN_PROGRESS_BACKUP_ENERGY);
						updateServerGUI();
					}
				});
	}

	private int getJobCount() {
		return serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> ACCEPTED_JOB_STATUSES.contains(job.getValue()) && isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.map(Map.Entry::getKey).map(Job::getJobId).collect(Collectors.toSet()).size();
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
