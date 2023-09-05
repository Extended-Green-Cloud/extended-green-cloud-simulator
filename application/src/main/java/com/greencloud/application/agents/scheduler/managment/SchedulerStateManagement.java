package com.greencloud.application.agents.scheduler.managment;

import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.FULL_JOBS_QUEUE_LOG;
import static com.greencloud.application.agents.scheduler.managment.logs.SchedulerManagementLog.JOB_TIME_ADJUSTED_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.utils.TimeUtils.postponeTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.PRE_EXECUTION;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.CREATED;
import static java.time.Duration.between;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
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
import com.greencloud.application.domain.job.JobWithPrice;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.ImmutableScheduledJobIdentity;
import com.greencloud.commons.domain.job.ScheduledJobIdentity;
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
	 * Method computes the priority for the given job
	 *
	 * @param clientJob job of interest
	 * @return double being the job priority
	 */
	public double getJobPriority(final ClientJob clientJob) {
		final double timeToDeadline = between(clientJob.getEndTime(), clientJob.getDeadline()).toMillis();
		return getDeadlinePercentage() * timeToDeadline + getCPUPriority() * clientJob.getEstimatedResources().getCpu();
	}

	/**
	 * Method sends the message and handles the communication with client
	 *
	 * @param message message that is to be sent
	 */
	public void sendStatusMessageToClient(final ACLMessage message) {
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
		var mappedQueue = new LinkedList<ScheduledJobIdentity>();

		queueCopy.iterator().forEachRemaining(el -> mappedQueue.add(ImmutableScheduledJobIdentity.builder()
				.jobId(el.getJobId())
				.clientName(el.getClientIdentifier())
				.build()));
		((SchedulerAgentNode) schedulerAgent.getAgentNode()).updateScheduledJobQueue(mappedQueue);
	}

	/**
	 * Method updates GUI with new weight values
	 */
	public void updateWeightsGUI() {
		if (nonNull(schedulerAgent.getAgentNode())) {
			((SchedulerAgentNode) schedulerAgent.getAgentNode()).updateCPUPriority(getCPUPriority());
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
			final Comparator<JobWithPrice> comparator = (cna1, cna2) ->
					(int) (cna1.getPriceForJob() - cna2.getPriceForJob());
			return compareReceivedOffers(offer1, offer2, JobWithPrice.class, comparator);
		};
	}

	/**
	 * Method performs clean up that removes the given job from Scheduler.
	 * It removes the job from client list and CNA map.
	 *
	 * @param job job to be removed
	 */
	public void handleJobCleanUp(final ClientJob job) {
		schedulerAgent.getClientJobs().remove(job);
		schedulerAgent.getCnaForJobMap().remove(job.getJobId());
	}

	/**
	 * Method performs post-processing after job failure
	 *
	 * @param job job to be cleaned up
	 */
	public void jobFailureCleanUp(final ClientJob job) {
		final List<String> jobsToRemove = getJobsToRemove(job);
		schedulerAgent.getClientJobs().entrySet().removeIf(entry -> jobsToRemove.contains(entry.getKey().getJobId()));
		schedulerAgent.getCnaForJobMap().entrySet().removeIf(entry -> jobsToRemove.contains(entry.getKey()));
	}

	private List<String> getJobsToRemove(final ClientJob job) {
		final Predicate<Map.Entry<ClientJob, JobExecutionStatusEnum>> shouldRemoveJob =
				jobEntry -> jobEntry.getKey().equals(job) && PRE_EXECUTION.getStatuses().contains(jobEntry.getValue());

		return schedulerAgent.getClientJobs().entrySet().stream()
				.filter(shouldRemoveJob)
				.map(entry -> entry.getKey().getJobId())
				.toList();
	}

	private boolean isJobAfterDeadline(final ClientJob job) {
		final Instant endAfterPostpone = postponeTime(job.getEndTime(), JOB_RETRY_MINUTES_ADJUSTMENT);
		return endAfterPostpone.isAfter(job.getDeadline());
	}

	private double getDeadlinePercentage() {
		return (double) schedulerAgent.getDeadlinePriority() / (schedulerAgent.getCPUPriority()
				+ schedulerAgent.getDeadlinePriority());
	}

	private double getCPUPriority() {
		return (double) schedulerAgent.getCPUPriority() / (schedulerAgent.getCPUPriority()
				+ schedulerAgent.getDeadlinePriority());
	}
}
