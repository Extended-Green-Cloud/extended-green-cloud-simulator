package com.greencloud.application.agents.server.management;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_ACCEPTED_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_FINISH_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_PROCESS_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_START_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static com.greencloud.application.utils.JobUtils.getJobSuccessRatio;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED_BY_SERVER_JOB_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.BACK_UP_POWER_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.JOB_ON_HOLD_STATUSES;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_SOURCE_SHORTAGE_PLANNED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.RUNNING_JOB_STATUSES;
import static com.greencloud.commons.job.JobResultType.ACCEPTED;
import static com.greencloud.commons.job.JobResultType.FAILED;
import static com.greencloud.commons.job.JobResultType.FINISH;
import static com.greencloud.commons.job.JobResultType.STARTED;
import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobStart;
import com.greencloud.application.agents.server.behaviour.powershortage.initiator.InitiateJobTransferInCloudNetwork;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.utils.AlgorithmUtils;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.JobResultType;
import com.gui.agents.ServerAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Set of utilities used to manage the internal state of the server agent
 */
public class ServerStateManagement {

	private static final Logger logger = LoggerFactory.getLogger(ServerStateManagement.class);
	private final ConcurrentMap<JobResultType, Long> jobCounters;
	private final ServerAgent serverAgent;

	public ServerStateManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
		this.jobCounters = Arrays.stream(JobResultType.values())
				.collect(Collectors.toConcurrentMap(status -> status, status -> 0L));
	}

	/**
	 * Method computes the available capacity (of given type) for the specified time frame.
	 *
	 * @param startDate    starting date
	 * @param endDate      end date
	 * @param jobToExclude (optional) job which will be excluded from the power calculation
	 * @param statusEnums  set of statuses of jobs that are taken into account while calculating in use power (optional)
	 * @return available power
	 */
	public synchronized int getAvailableCapacity(final Instant startDate, final Instant endDate,
			final JobInstanceIdentifier jobToExclude, final Set<ExecutionJobStatusEnum> statusEnums) {
		final Set<ExecutionJobStatusEnum> statuses = Objects.isNull(statusEnums) ?
				ACCEPTED_BY_SERVER_JOB_STATUSES :
				statusEnums;
		final Set<ClientJob> jobsOfInterest = serverAgent.getServerJobs().keySet().stream()
				.filter(job -> Objects.isNull(jobToExclude) || !mapToJobInstanceId(job).equals(jobToExclude))
				.filter(job -> statuses.contains(serverAgent.getServerJobs().get(job))).collect(toSet());
		final int maxUsedPower = AlgorithmUtils.getMaximumUsedPowerDuringTimeStamp(jobsOfInterest, startDate, endDate);
		return serverAgent.getCurrentMaximumCapacity() - maxUsedPower;
	}

	/**
	 * Method performs job finishing action
	 *
	 * @param jobToFinish job to be finished
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 */
	public void finishJobExecution(final ClientJob jobToFinish, final boolean informCNA) {
		finishJobExecutionWithResult(jobToFinish, informCNA, FINISH);
	}

	/**
	 * Method performs job finishing action with a specified state
	 *
	 * @param jobToFinish job to be finished
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 */
	public void finishJobExecutionWithResult(final ClientJob jobToFinish, final boolean informCNA,
			JobResultType resultType) {
		final ExecutionJobStatusEnum executionJobStatusEnum = serverAgent.getServerJobs().get(jobToFinish);

		sendFinishInformation(jobToFinish, informCNA);
		updateStateAfterJobIsDone(jobToFinish, resultType);

		if (executionJobStatusEnum.equals(IN_PROGRESS_BACKUP_ENERGY) || executionJobStatusEnum.equals(
				IN_PROGRESS_BACKUP_ENERGY_PLANNED)) {
			final Map<ClientJob, ExecutionJobStatusEnum> jobsWithinTimeStamp = serverAgent.getServerJobs().entrySet()
					.stream()
					.filter(job -> isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(),
							getCurrentTime())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			supplyJobsWithBackupPower(jobsWithinTimeStamp);
		}
		updateServerGUI();
	}

	/**
	 * Method resends the job transfer request to parent Cloud Network
	 *
	 * @param jobInstanceId      job that is to be transferred
	 * @param powerShortageStart time when the power shortage starts
	 * @param request            initial green source request
	 */
	public void passTransferRequestToCloudNetwork(final JobInstanceIdentifier jobInstanceId,
			final Instant powerShortageStart, final ACLMessage request) {
		final PowerShortageJob jobTransfer = JobMapper.mapToPowerShortageJob(jobInstanceId, powerShortageStart);
		final AID cloudNetwork = serverAgent.getOwnerCloudNetworkAgent();
		final ACLMessage transferMessage = preparePowerShortageTransferRequest(jobTransfer, cloudNetwork);

		serverAgent.addBehaviour(
				new InitiateJobTransferInCloudNetwork(serverAgent, transferMessage, request, jobTransfer));
	}

	/**
	 * Method increments the counter of jobs
	 *
	 * @param jobInstanceId job identifier
	 * @param type          type of counter to increment
	 */
	public void incrementJobCounter(final JobInstanceIdentifier jobInstanceId, final JobResultType type) {
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		jobCounters.computeIfPresent(type, (key, val) -> val += 1);

		switch (type) {
			case FAILED -> logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED));
			case ACCEPTED -> logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED));
			case STARTED -> logger.info(COUNT_JOB_START_LOG, jobInstanceId, jobCounters.get(STARTED),
					jobCounters.get(ACCEPTED));
			case FINISH ->
					logger.info(COUNT_JOB_FINISH_LOG, jobInstanceId, jobCounters.get(FINISH), jobCounters.get(STARTED));
		}
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
	public ClientJob divideJobForPowerShortage(final ClientJob job, final Instant powerShortageStart) {
		if (powerShortageStart.isAfter(job.getStartTime()) && !powerShortageStart.equals(job.getStartTime())) {
			final ClientJob affectedJobInstance = JobMapper.mapToJobNewStartTime(job, powerShortageStart);
			final ClientJob notAffectedJobInstance = JobMapper.mapToJobNewEndTime(job, powerShortageStart);
			final ExecutionJobStatusEnum currentJobStatus = serverAgent.getServerJobs().get(job);

			serverAgent.getServerJobs().remove(job);
			serverAgent.getServerJobs().put(affectedJobInstance, ExecutionJobStatusEnum.ON_HOLD_TRANSFER);
			serverAgent.getServerJobs().put(notAffectedJobInstance, currentJobStatus);

			serverAgent.addBehaviour(HandleJobStart.createFor(serverAgent, affectedJobInstance, false, true));
			serverAgent.addBehaviour(HandleJobFinish.createFor(serverAgent, notAffectedJobInstance, false));

			if (getCurrentTime().isBefore(notAffectedJobInstance.getStartTime())) {
				serverAgent.addBehaviour(HandleJobStart.createFor(serverAgent, notAffectedJobInstance, true, false));
			}

			return affectedJobInstance;
		} else {
			serverAgent.getServerJobs().replace(job, ExecutionJobStatusEnum.ON_HOLD_TRANSFER);
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
			final double successRatio = getJobSuccessRatio(jobCounters.get(ACCEPTED), jobCounters.get(FAILED));

			serverAgentNode.updateMaximumCapacity(serverAgent.getCurrentMaximumCapacity(),
					getCurrentPowerInUseForServer());
			serverAgentNode.updateJobsCount(getJobCount());
			serverAgentNode.updateClientNumber(getClientNumber());
			serverAgentNode.updateIsActive(getIsActiveState());
			serverAgentNode.updateTraffic(getCurrentPowerInUseForServer());
			serverAgentNode.updateBackUpTraffic(getCurrentBackUpPowerInUseForServer());
			serverAgentNode.updateJobsOnHoldCount(getOnHoldJobsCount());
			serverAgentNode.updateCurrentJobSuccessRatio(successRatio);
			writeStateToDatabase();
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

	/**
	 * Method informs CNA that the status of given job has changed
	 *
	 * @param jobInstance job which status has changed
	 * @param type        new status type
	 */
	public void informCNAAboutStatusChange(final JobInstanceIdentifier jobInstance, final String type) {
		final ACLMessage information = prepareJobStatusMessageForCNA(jobInstance, type, serverAgent);
		serverAgent.send(information);
	}

	/**
	 * Method retrieves the addresses of green sources that are marked as active
	 *
	 * @return set of active green sources
	 */
	public Set<AID> getOwnedActiveGreenSources() {
		return serverAgent.getOwnedGreenSources().entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(toSet());
	}

	public ConcurrentMap<JobResultType, Long> getJobCounters() {
		return jobCounters;
	}

	private void writeStateToDatabase() {
		final double trafficOverall = serverAgent.getCurrentMaximumCapacity() == 0 ?
				0 :
				((double) getCurrentPowerInUseForServer()) / serverAgent.getCurrentMaximumCapacity();
		final double backUpPowerOverall = serverAgent.getCurrentMaximumCapacity() == 0 ?
				0 :
				((double) getCurrentBackUpPowerInUseForServer()) / serverAgent.getCurrentMaximumCapacity();

		final ServerMonitoringData serverMonitoringData = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(serverAgent.getCurrentMaximumCapacity())
				.currentTraffic(trafficOverall)
				.currentBackUpPowerUsage(backUpPowerOverall)
				.successRatio(getJobSuccessRatio(jobCounters.get(ACCEPTED), jobCounters.get(FAILED)))
				.build();
		serverAgent.writeMonitoringData(SERVER_MONITORING, serverMonitoringData);
	}

	private void sendFinishInformation(final ClientJob jobToFinish, final boolean informCNA) {
		final List<AID> receivers = informCNA ?
				List.of(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()),
						serverAgent.getOwnerCloudNetworkAgent()) :
				Collections.singletonList(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()));
		final ACLMessage finishJobMessage = prepareJobFinishMessage(jobToFinish.getJobId(), jobToFinish.getStartTime(),
				receivers);

		serverAgent.send(finishJobMessage);
	}

	private void updateStateAfterJobIsDone(final ClientJob jobToBeDone, JobResultType jobResultType) {
		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(jobToBeDone);
		incrementJobCounter(jobInstance, jobResultType);
		if (isJobUnique(jobToBeDone.getJobId(), serverAgent.getServerJobs())) {
			serverAgent.getGreenSourceForJobMap().remove(jobToBeDone.getJobId());
			updateClientNumberGUI();
		}
		serverAgent.getServerJobs().remove(jobToBeDone);
	}

	private void supplyJobsWithBackupPower(final Map<ClientJob, ExecutionJobStatusEnum> jobEntries) {
		jobEntries.entrySet().stream()
				.filter(job -> job.getValue().equals(ON_HOLD_SOURCE_SHORTAGE_PLANNED) || job.getValue()
						.equals(ON_HOLD_SOURCE_SHORTAGE))
				.forEach(jobEntry -> {
					final ClientJob job = jobEntry.getKey();
					if (getAvailableCapacity(job.getStartTime(), job.getEndTime(), mapToJobInstanceId(job),
							BACK_UP_POWER_STATUSES) >= job.getPower()) {
						final ExecutionJobStatusEnum status = jobEntry.getValue().equals(ON_HOLD_SOURCE_SHORTAGE) ?
								IN_PROGRESS_BACKUP_ENERGY :
								IN_PROGRESS_BACKUP_ENERGY_PLANNED;
						MDC.put(MDC_JOB_ID, job.getJobId());
						logger.info("Supplying job {} with back up power", job.getJobId());
						serverAgent.getServerJobs().replace(job, status);
						updateServerGUI();
					}
				});
	}

	private int getJobCount() {
		return serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> RUNNING_JOB_STATUSES.contains(job.getValue()) && isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
				.map(Map.Entry::getKey).map(ClientJob::getJobId).collect(toSet()).size();
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
				.filter(job -> JOB_ON_HOLD_STATUSES.contains(job.getValue()) && isWithinTimeStamp(
						job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime())).toList().size();
	}

	private boolean getIsActiveState() {
		return getCurrentPowerInUseForServer() > 0 || getCurrentBackUpPowerInUseForServer() > 0;
	}
}
