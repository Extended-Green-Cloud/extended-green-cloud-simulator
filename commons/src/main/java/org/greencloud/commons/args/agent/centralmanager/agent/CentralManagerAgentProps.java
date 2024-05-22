package org.greencloud.commons.args.agent.centralmanager.agent;

import static java.time.Instant.now;
import static java.util.Comparator.comparingDouble;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.ToDoubleFunction;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Central Manager Agent
 */
@Getter
@Setter
public class CentralManagerAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(CentralManagerAgentProps.class);

	private static final int PROCESSING_TIME_ADJUSTMENT = 3000;
	private static final int DEADLINE_TIME_ADJUSTMENT = 1000;

	protected PriorityBlockingQueue<ClientJob> jobsToBeExecuted;
	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> clientJobs;
	protected ConcurrentMap<ClientJob, RuleSetFacts> priorityFacts;
	protected ConcurrentMap<String, Integer> ruleSetForJob;
	protected ConcurrentMap<String, AID> rmaForJobMap;
	protected List<AID> availableRegionalManagers;
	protected int maximumQueueSize;
	protected int pollingBatchSize;
	protected Instant lastPollingTime;

	public CentralManagerAgentProps(final String agentName) {
		super(CENTRAL_MANAGER, agentName);
	}

	/**
	 * Constructor
	 *
	 * @param agentName        name of the agent
	 * @param maximumQueueSize size of the scheduling queue
	 * @param pollingBatchSize size of scheduling queue polling
	 */
	public CentralManagerAgentProps(final String agentName, final int maximumQueueSize, final int pollingBatchSize) {
		this(agentName);
		this.maximumQueueSize = maximumQueueSize;
		this.pollingBatchSize = pollingBatchSize;

		this.lastPollingTime = now();
		this.clientJobs = new ConcurrentHashMap<>();
		this.rmaForJobMap = new ConcurrentHashMap<>();
		this.availableRegionalManagers = new ArrayList<>();
		this.ruleSetForJob = new ConcurrentHashMap<>();
		this.priorityFacts = new ConcurrentHashMap<>();
	}

	/**
	 * Method adds new client job
	 *
	 * @param job     job that is to be added
	 * @param ruleSet rule set with which the job is to be handled
	 * @param status  status of the job
	 */
	public void addJob(final ClientJob job, final Integer ruleSet, final JobExecutionStatusEnum status) {
		clientJobs.put(job, status);
		ruleSetForJob.put(job.getJobId(), ruleSet);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 * @return boolean indicating if rule set should be removed from controller
	 */
	public int removeJob(final ClientJob job) {
		clientJobs.remove(job);
		return ruleSetForJob.remove(job.getJobId());
	}

	/**
	 * Method initializes priority queue
	 */
	public void setUpPriorityQueue(final ToDoubleFunction<ClientJob> getJobPriority) {
		this.jobsToBeExecuted = new PriorityBlockingQueue<>(maximumQueueSize, comparingDouble(getJobPriority));
	}

	/**
	 * Method returns estimated job finish if it was to be scheduled now.
	 *
	 * @param job job for which finish time is to be estimated
	 * @return estimated job finish
	 */
	public Instant getEstimatedJobEndFromNow(final ClientJob job) {
		final Instant newAdjustedStart = getCurrentTime().plusMillis(PROCESSING_TIME_ADJUSTMENT);
		return newAdjustedStart.plusMillis(job.getDuration());
	}

	/**
	 * Method estimated if job will be executed possibly after deadline.
	 *
	 * @param job job that is to be executed
	 * @return boolean estimation
	 */
	public boolean isPossiblyAfterDeadline(final ClientJob job) {
		final Instant jobEstimatedEnd = getEstimatedJobEndFromNow(job);
		return jobEstimatedEnd.isAfter(job.getDeadline().minusMillis(DEADLINE_TIME_ADJUSTMENT));
	}
}
