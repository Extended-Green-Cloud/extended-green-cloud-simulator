package org.greencloud.commons.args.agent.scheduler.agent;

import static java.util.Comparator.comparingDouble;
import static org.greencloud.commons.mapper.JobMapper.mapToJobWithNewTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.ToDoubleFunction;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.constants.LoggingConstants;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.utils.time.TimeScheduler;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Scheduler Agent
 */
@Getter
@Setter
public class SchedulerAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(SchedulerAgentProps.class);

	protected PriorityBlockingQueue<ClientJob> jobsToBeExecuted;
	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> clientJobs;
	protected ConcurrentMap<String, Integer> strategyForJob;
	protected ConcurrentMap<String, AID> cnaForJobMap;
	protected List<AID> availableCloudNetworks;
	protected int deadlinePriority;
	protected int cpuPriority;
	protected int maximumQueueSize;

	public SchedulerAgentProps(final String agentName) {
		super(AgentType.SCHEDULER, agentName);
	}

	/**
	 * Constructor
	 *
	 * @param agentName        name of the agent
	 * @param deadlinePriority weight of priority value assigned to job based on its deadline
	 * @param cpuPriority      weight of priority value assigned to job based on its CPU requirement
	 * @param maximumQueueSize size of the scheduling queue
	 */
	public SchedulerAgentProps(final String agentName, final int deadlinePriority, final int cpuPriority,
			final int maximumQueueSize) {
		this(agentName);
		this.deadlinePriority = deadlinePriority;
		this.cpuPriority = cpuPriority;
		this.maximumQueueSize = maximumQueueSize;

		this.clientJobs = new ConcurrentHashMap<>();
		this.cnaForJobMap = new ConcurrentHashMap<>();
		this.availableCloudNetworks = new ArrayList<>();
		this.strategyForJob = new ConcurrentHashMap<>();
	}

	/**
	 * Method adds new client job
	 *
	 * @param job      job that is to be added
	 * @param strategy strategy with which the job is to be handled
	 * @param status   status of the job
	 */
	public void addJob(final ClientJob job, final Integer strategy, final JobExecutionStatusEnum status) {
		clientJobs.put(job, status);
		strategyForJob.put(job.getJobId(), strategy);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 * @return boolean indicating if strategy should be removed from cotnroller
	 */
	public int removeJob(final ClientJob job) {
		clientJobs.remove(job);
		return strategyForJob.remove(job.getJobId());
	}

	/**
	 * Method initializes priority queue
	 */
	public void setUpPriorityQueue(final ToDoubleFunction<ClientJob> getJobPriority) {
		this.jobsToBeExecuted = new PriorityBlockingQueue<>(maximumQueueSize, comparingDouble(getJobPriority));
	}

	/**
	 * Method swaps existing job instance with the new one
	 *
	 * @param newInstance  new job instance
	 * @param prevInstance old job instance
	 */
	public void swapJobInstances(final ClientJob newInstance, final ClientJob prevInstance) {
		clientJobs.remove(prevInstance);
		clientJobs.put(newInstance, JobExecutionStatusEnum.CREATED);
	}

	/**
	 * Method postpones the job execution by substituting the previous instance with the one
	 * having adjusted time frames
	 *
	 * @param job             job to be postponed
	 * @param postponeMinutes minutes by which job is to be postponed
	 * @return true if the operation was successful, false if the job couldn't be postponed due to its deadline
	 */
	public boolean postponeJobExecution(final ClientJob job, final int postponeMinutes) {
		if (isJobAfterDeadline(job, postponeMinutes)) {
			return false;
		}
		final ClientJob adjustedJob = mapToJobWithNewTime(job,
				TimeScheduler.postponeTime(job.getStartTime(), postponeMinutes),
				TimeScheduler.postponeTime(job.getEndTime(), postponeMinutes));
		swapJobInstances(adjustedJob, job);

		if (!jobsToBeExecuted.offer(adjustedJob)) {
			MDC.put(LoggingConstants.MDC_JOB_ID, job.getJobId());
			logger.info("Postponed job {} was successfully added but the "
					+ "queue reached the threshold. Consider adjusting the queue size!", job.getJobId());
		}
		return true;
	}

	/**
	 * Method computes percentage of job deadline weight
	 *
	 * @return deadline percentage
	 */
	public double getDeadlinePercentage() {
		return (double) deadlinePriority / (deadlinePriority + cpuPriority);
	}

	/**
	 * Method computes percentage of job CPU weight
	 *
	 * @return CPU percentage
	 */
	public double getCPUPercentage() {
		return (double) cpuPriority / (deadlinePriority + cpuPriority);
	}

	private boolean isJobAfterDeadline(final ClientJob job, final int postponeMinutes) {
		final Instant endAfterPostpone = TimeScheduler.postponeTime(job.getEndTime(), postponeMinutes);
		return endAfterPostpone.isAfter(job.getDeadline());
	}
}
