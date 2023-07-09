package com.greencloud.application.agents.scheduler;

import static com.google.common.collect.Multimaps.synchronizedMultimap;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.SCHEDULER;
import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.scheduler.managment.SchedulerAdaptationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Scheduler Agent
 */
public abstract class AbstractSchedulerAgent extends AbstractAgent {

	protected PriorityBlockingQueue<ClientJob> jobsToBeExecuted;
	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> clientJobs;
	protected ConcurrentMap<String, AID> cnaForJobMap;
	protected List<AID> availableCloudNetworks;
	protected Multimap<String, ClientJob> jobParts;
	protected Set<String> failedJobs;
	protected Set<String> jobsExecutedInCloud;
	protected ConcurrentMap<String, Integer> jobPostpones;

	protected int deadlinePriority;
	protected int powerPriority;
	protected int maximumQueueSize;
	protected int jobSplitThreshold;
	protected int splittingFactor;

	/**
	 * Default constructor.
	 */
	protected AbstractSchedulerAgent() {
		super();

		this.jobParts = synchronizedMultimap(ArrayListMultimap.create());
		this.clientJobs = new ConcurrentHashMap<>();
		this.cnaForJobMap = new ConcurrentHashMap<>();
		this.availableCloudNetworks = new ArrayList<>();
		this.failedJobs = new HashSet<>();
		this.jobsExecutedInCloud = new HashSet<>();
		this.jobPostpones = new ConcurrentHashMap<>();
		this.agentType = SCHEDULER;
	}

	/**
	 * Method initializes priority queue
	 */
	public void setUpPriorityQueue() {
		this.jobsToBeExecuted = new PriorityBlockingQueue<>(maximumQueueSize,
				comparingDouble(job -> manage().getJobPriority(job)));
	}

	public SchedulerAdaptationManagement adapt() {
		return (SchedulerAdaptationManagement) agentManagementServices.get(ADAPTATION_MANAGEMENT);
	}

	public SchedulerStateManagement manage() {
		return (SchedulerStateManagement) agentManagementServices.get(STATE_MANAGEMENT);
	}

	public PriorityBlockingQueue<ClientJob> getJobsToBeExecuted() {
		return jobsToBeExecuted;
	}

	public ConcurrentMap<ClientJob, JobExecutionStatusEnum> getClientJobs() {
		return clientJobs;
	}

	public ConcurrentMap<String, AID> getCnaForJobMap() {
		return cnaForJobMap;
	}

	public List<AID> getAvailableCloudNetworks() {
		return availableCloudNetworks;
	}

	public Multimap<String, ClientJob> getJobParts() {
		return jobParts;
	}

	public Set<String> getFailedJobs() {
		return failedJobs;
	}

	public Set<String> getJobsExecutedInCloud() {
		return jobsExecutedInCloud;
	}

	public int getJobSplitThreshold() {
		return jobSplitThreshold;
	}

	public int getSplittingFactor() {
		return splittingFactor;
	}

	public int getDeadlinePriority() {
		return deadlinePriority;
	}

	public void setDeadlinePriority(int deadlinePriority) {
		this.deadlinePriority = deadlinePriority;
	}

	public int getPowerPriority() {
		return powerPriority;
	}

	public void setPowerPriority(int powerPriority) {
		this.powerPriority = powerPriority;
	}

	public ConcurrentMap<String, Integer> getJobPostpones() {
		return jobPostpones;
	}

	@VisibleForTesting
	public void setMaximumQueueSize(int maximumQueueSize) {
		this.maximumQueueSize = maximumQueueSize;
	}
}
