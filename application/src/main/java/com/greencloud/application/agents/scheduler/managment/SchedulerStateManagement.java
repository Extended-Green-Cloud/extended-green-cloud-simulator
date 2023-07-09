package com.greencloud.application.agents.scheduler.managment;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.logs.JobSchedulingListenerLog.JOB_CANCELLATION_LOG;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_START_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.FULL_JOBS_QUEUE_LOG;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.JOB_TIME_ADJUSTED_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToClientJobRealTime;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.postponeTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.PRE_EXECUTION;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.CREATED;
import static java.time.Duration.between;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.AbstractStateManagement;
import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.job.cancellation.InitiateJobCancellation;
import com.greencloud.application.domain.job.JobWithComponentSuccess;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;
import com.gui.agents.SchedulerAgentNode;

import jade.lang.acl.ACLMessage;

/**
 * Set of utilities used to manage the state of Scheduler Agent
 */
public class SchedulerStateManagement extends AbstractStateManagement {

	private static final Logger logger = getLogger(SchedulerStateManagement.class);
	private final SchedulerAgent schedulerAgent;

	/**
	 * Default constructor
	 *
	 * @param schedulerAgent parent scheduler agent
	 */
	public SchedulerStateManagement(final SchedulerAgent schedulerAgent) {
		this.schedulerAgent = schedulerAgent;
	}

	/**
	 * Method used in updating GUI associated with scheduler
	 */
	@Override
	public void updateGUI() {
		schedulerAgent.getGuiController()
				.updateActiveInCloudJobsCountByValue(schedulerAgent.getJobsExecutedInCloud().size());
	}

	/**
	 * Method computes the priority for the given job
	 *
	 * @param clientJob job of interest
	 * @return double being the job priority
	 */
	public double getJobPriority(final ClientJob clientJob) {
		final double timeToDeadline = between(clientJob.getEndTime(), clientJob.getDeadline()).toMillis();
		return getDeadlinePercentage() * timeToDeadline + getPowerPercentage() * clientJob.getPower();
	}

	/**
	 * Method sends the message and handles the communication with client
	 *
	 * @param message message that is to be sent
	 */
	public void sendStatusMessageToClient(final ACLMessage message, final String jobId) {
		schedulerAgent.send(message);
	}

	/**
	 * Method postpones the job execution by substituting the previous instance with the one
	 * having adjusted time frames
	 *
	 * @param job job to be postponed
	 * @return true if the operation was successful, false if the job couldn't be postponed due to its deadline
	 */
	public boolean postponeJobExecution(final ClientJob job) {
		if (isJobAfterDeadline(job)) {
			return false;
		}
		final ClientJob adjustedJob = mapToJobWithNewTime(job,
				postponeTime(job.getStartTime(), JOB_RETRY_MINUTES_ADJUSTMENT),
				postponeTime(job.getEndTime(), JOB_RETRY_MINUTES_ADJUSTMENT));
		swapJobInstances(adjustedJob, job);

		if (!schedulerAgent.getJobsToBeExecuted().offer(adjustedJob)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.info(FULL_JOBS_QUEUE_LOG, job.getJobId());
			updateJobQueueGUI();
		}
		return true;
	}

	/**
	 * Method updates GUI with new job queue
	 */
	public void updateJobQueueGUI() {
		var queueCopy = new LinkedList<>(schedulerAgent.getJobsToBeExecuted());
		var mappedQueue = new LinkedList<ClientJob>();

		queueCopy.iterator().forEachRemaining(el -> mappedQueue.add(mapToClientJobRealTime(el)));
		((SchedulerAgentNode) schedulerAgent.getAgentNode()).updateScheduledJobQueue(mappedQueue);
	}

	/**
	 * Method updates GUI with new weight values
	 */
	public void updateWeightsGUI() {
		if (nonNull(schedulerAgent.getAgentNode())) {
			((SchedulerAgentNode) schedulerAgent.getAgentNode()).updatePowerPriority(getPowerPercentage());
			((SchedulerAgentNode) schedulerAgent.getAgentNode()).updateDeadlinePriority(getDeadlinePercentage());
		}
	}

	/**
	 * Method swaps existing job instance with the new one that has adjusted time frames
	 *
	 * @param newInstance  new job instance
	 * @param prevInstance old job instance
	 */
	public void swapJobInstances(final ClientJob newInstance, final ClientJob prevInstance) {
		schedulerAgent.getClientJobs().remove(prevInstance);
		MDC.put(MDC_JOB_ID, newInstance.getJobId());
		logger.info(JOB_TIME_ADJUSTED_LOG, newInstance.getJobId(), newInstance.getStartTime(),
				newInstance.getEndTime());
		schedulerAgent.getClientJobs().put(newInstance, CREATED);
	}

	/**
	 * Method defines comparator used to evaluate offers for job execution proposed by Cloud Networks
	 *
	 * @return method comparator returns:
	 * <p> val > 0 - if the offer1 is better</p>
	 * <p> val = 0 - if both offers are equivalently good</p>
	 * <p> val < 0 - if the offer2 is better</p>
	 */
	public BiFunction<ACLMessage, ACLMessage, Integer> offerComparator() {
		return (offer1, offer2) -> {
			final Comparator<JobWithComponentSuccess> comparator = (cna1, cna2) ->
					(int) (cna1.getSuccessRatio() - cna2.getSuccessRatio());
			return compareReceivedOffers(offer1, offer2, JobWithComponentSuccess.class, comparator);
		};
	}

	/**
	 * Method performs clean up that removes the given job from Scheduler.
	 * It removes the job from client list, CNA map and also, if a job is a job part, then it also removes it from
	 * job part map.
	 *
	 * @param job     job to be removed
	 * @param isInCNA flag indicating if the job was executed by CNA
	 */
	public void handleJobCleanUp(final ClientJob job, final boolean isInCNA) {
		final String originalJobId = job.getJobId().split("#")[0];

		schedulerAgent.getClientJobs().remove(job);
		schedulerAgent.getJobPostpones().remove(job.getJobId());

		if (isInCNA) {
			schedulerAgent.getCnaForJobMap().remove(job.getJobId());
		} else {
			schedulerAgent.getJobsExecutedInCloud().remove(job.getJobId());
		}

		final Collection<ClientJob> jobParts = schedulerAgent.getJobParts().get(originalJobId);
		jobParts.stream()
				.filter(jobPart -> jobPart.getJobId().equals(job.getJobId()))
				.findFirst()
				.ifPresent(jobPart -> schedulerAgent.getJobParts().remove(originalJobId, jobPart));
	}

	/**
	 * Method performs post-processing after job failure
	 *
	 * @param job job to be cleaned up
	 */
	public void jobFailureCleanUp(final ClientJob job) {
		final List<String> jobsToRemove = getJobsToRemove(job);
		var originalJobId = job.getJobId().split("#")[0];

		schedulerAgent.getClientJobs().entrySet().removeIf(entry -> jobsToRemove.contains(entry.getKey().getJobId()));
		schedulerAgent.getJobPostpones().entrySet().removeIf(entry -> jobsToRemove.contains(entry.getKey()));
		schedulerAgent.getCnaForJobMap().entrySet().removeIf(entry -> jobsToRemove.contains(entry.getKey()));
		schedulerAgent.getJobsExecutedInCloud().removeIf(jobsToRemove::contains);
		schedulerAgent.getJobParts().entries().removeIf(entry -> entry.getKey().equals(originalJobId)
				&& jobsToRemove.contains(entry.getValue().getJobId()));

		if (schedulerAgent.getClientJobs().keySet().stream().anyMatch(isJobIdEqual(job))) {
			initiateJobCancellation(originalJobId);
		}
	}

	/**
	 * Method evaluates if postponing a job will make it reach the deadline
	 *
	 * @param job - job that is to be postponed
	 * @return boolean indicating if postponing will exceed the deadline
	 */
	public boolean isJobAfterDeadline(final ClientJob job) {
		final Instant endAfterPostpone = postponeTime(job.getEndTime(), JOB_RETRY_MINUTES_ADJUSTMENT);
		return endAfterPostpone.isAfter(job.getDeadline());
	}

	/**
	 * Method verifies if, starting from now, the job can be fully executed
	 *
	 * @param job job to be executed
	 * @return boolean indicating if job can be fully executed
	 */
	public boolean canJobBeFullyExecutedBeforeDeadline(final ClientJob job) {
		final long jobDuration = MILLIS.between(job.getStartTime(), job.getEndTime());
		final Instant jobExpectedFinishTime = getCurrentTime().plusMillis(jobDuration + JOB_START_ADJUSTMENT);

		return !jobExpectedFinishTime.isAfter(job.getDeadline());
	}

	private Predicate<ClientJob> isJobIdEqual(final ClientJob job) {
		return jobPart -> job.getJobId().split("#")[0].equals(jobPart.getJobId().split("#")[0]);
	}

	private List<String> getJobsToRemove(final ClientJob job) {
		final Predicate<Map.Entry<ClientJob, JobExecutionStatusEnum>> shouldRemoveJob =
				jobEntry -> jobEntry.getKey().equals(job) || (isJobIdEqual(job).test(jobEntry.getKey())
						&& PRE_EXECUTION.getStatuses().contains(jobEntry.getValue()));

		return schedulerAgent.getClientJobs().entrySet().stream()
				.filter(shouldRemoveJob)
				.map(entry -> entry.getKey().getJobId())
				.toList();
	}

	private void initiateJobCancellation(String originalJobId) {
		if (schedulerAgent.getFailedJobs().contains(originalJobId)) {
			// do nothing, job cancellation already initiated for that job
			return;
		}

		MDC.put(MDC_JOB_ID, originalJobId);
		logger.info(JOB_CANCELLATION_LOG);
		schedulerAgent.getFailedJobs().add(originalJobId);
		schedulerAgent.addBehaviour(InitiateJobCancellation.create(schedulerAgent, originalJobId));
	}

	private double getDeadlinePercentage() {
		return (double) schedulerAgent.getDeadlinePriority() / (schedulerAgent.getPowerPriority()
				+ schedulerAgent.getDeadlinePriority());
	}

	private double getPowerPercentage() {
		return (double) schedulerAgent.getPowerPriority() / (schedulerAgent.getPowerPriority()
				+ schedulerAgent.getDeadlinePriority());
	}
}
