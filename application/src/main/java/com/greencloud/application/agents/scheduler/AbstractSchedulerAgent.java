package com.greencloud.application.agents.scheduler;

import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.QUEUE_CAPACITY_THRESHOLD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Scheduler Agent
 */
public abstract class AbstractSchedulerAgent extends AbstractAgent {

	protected PriorityBlockingQueue<ClientJob> jobsToBeExecuted;
	protected ConcurrentMap<ClientJob, JobStatusEnum> clientJobs;
	protected ConcurrentMap<String, AID> cnaForJobMap;
	protected List<AID> availableCloudNetworks;

	protected SchedulerConfigurationManagement configManagement;
	protected SchedulerStateManagement stateManagement;

	/**
	 * Default constructor.
	 */
	protected AbstractSchedulerAgent() {
		super.setup();

		this.jobsToBeExecuted = new PriorityBlockingQueue<>(QUEUE_CAPACITY_THRESHOLD,
				Comparator.comparingDouble(job -> configManagement.getJobPriority(job)));
		this.clientJobs = new ConcurrentHashMap<>();
		this.cnaForJobMap = new ConcurrentHashMap<>();
		this.availableCloudNetworks = new ArrayList<>();
	}

	/**
	 * @return jobs that are to be introduced in Cloud Network
	 */
	public PriorityBlockingQueue<ClientJob> getJobsToBeExecuted() {
		return jobsToBeExecuted;
	}

	/**
	 * @return jobs introduced to the Scheduler Agent
	 */
	public ConcurrentMap<ClientJob, JobStatusEnum> getClientJobs() {
		return clientJobs;
	}

	/**
	 * @return cloud networks assigned for specific job execution
	 */
	public ConcurrentMap<String, AID> getCnaForJobMap() {
		return cnaForJobMap;
	}

	/**
	 * @return list of available cloud networks
	 */
	public List<AID> getAvailableCloudNetworks() {
		return availableCloudNetworks;
	}

	/**
	 * @return configuration manager
	 */
	public SchedulerConfigurationManagement config() {
		return configManagement;
	}

	/**
	 * @return state manager
	 */
	public SchedulerStateManagement manage() {
		return stateManagement;
	}
}
