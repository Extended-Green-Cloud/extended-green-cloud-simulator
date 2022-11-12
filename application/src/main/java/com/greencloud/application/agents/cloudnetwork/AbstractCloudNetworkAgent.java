package com.greencloud.application.agents.cloudnetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.cloudnetwork.management.CloudNetworkStateManagement;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Cloud Network Agent
 */
public abstract class AbstractCloudNetworkAgent extends AbstractAgent {

	protected transient CloudNetworkStateManagement stateManagement;
	protected Map<ClientJob, JobStatusEnum> networkJobs;
	protected Map<String, AID> serverForJobMap;
	protected AtomicLong completedJobs;
	protected List<AID> ownedServers;
	protected AID scheduler;

	AbstractCloudNetworkAgent() {
		super.setup();
	}

	/**
	 * Method run on agent start. It initializes the Cloud Network Agent data with default values
	 */
	@Override
	protected void setup() {
		super.setup();

		serverForJobMap = new HashMap<>();
		networkJobs = new HashMap<>();
		completedJobs = new AtomicLong(0L);
	}

	public Map<String, AID> getServerForJobMap() {
		return serverForJobMap;
	}

	public Map<ClientJob, JobStatusEnum> getNetworkJobs() {
		return networkJobs;
	}

	public Long completedJob() {
		return completedJobs.incrementAndGet();
	}

	public List<AID> getOwnedServers() {
		return ownedServers;
	}

	public void setOwnedServers(List<AID> ownedServers) {
		this.ownedServers = ownedServers;
	}

	public AID getScheduler() {
		return scheduler;
	}

	public void setScheduler(AID scheduler) {
		this.scheduler = scheduler;
	}

	public CloudNetworkStateManagement manage() {
		return stateManagement;
	}
}
