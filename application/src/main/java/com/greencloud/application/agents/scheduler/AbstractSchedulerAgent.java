package com.greencloud.application.agents.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.scheduler.managment.SchedulerConfigurationManagement;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.commons.job.ClientJob;

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

	protected Multimap<String, ClientJob> jobParts;

	/**
	 * Default constructor.
	 */
	protected AbstractSchedulerAgent() {
		super.setup();
		this.clientJobs = new ConcurrentHashMap<>();
		this.cnaForJobMap = new ConcurrentHashMap<>();
		this.availableCloudNetworks = new ArrayList<>();
		this.jobParts = ArrayListMultimap.create();
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
	 * @return multimap of jobs parts
	 */
	public Multimap<String, ClientJob> getJobParts() {
		return jobParts;
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
