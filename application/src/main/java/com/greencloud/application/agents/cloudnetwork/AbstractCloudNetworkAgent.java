package com.greencloud.application.agents.cloudnetwork;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.CNA;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.cloudnetwork.management.CloudNetworkStateManagement;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.AID;

/**
 * Abstract agent class storing the data regarding Cloud Network Agent
 */
public abstract class AbstractCloudNetworkAgent extends AbstractAgent {

	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> networkJobs;
	protected ConcurrentMap<String, AID> serverForJobMap;
	protected ConcurrentMap<AID, Boolean> ownedServers;
	protected ConcurrentMap<AID, Integer> weightsForServersMap;
	protected AID scheduler;

	AbstractCloudNetworkAgent() {
		super();

		this.agentType = CNA;
		this.serverForJobMap = new ConcurrentHashMap<>();
		this.networkJobs = new ConcurrentHashMap<>();
		this.ownedServers = new ConcurrentHashMap<>();
		this.weightsForServersMap = new ConcurrentHashMap<>();
	}

	public CloudNetworkStateManagement manage() {
		return (CloudNetworkStateManagement) agentManagementServices.get(STATE_MANAGEMENT);
	}

	public ConcurrentMap<String, AID> getServerForJobMap() {
		return serverForJobMap;
	}

	public ConcurrentMap<ClientJob, JobExecutionStatusEnum> getNetworkJobs() {
		return networkJobs;
	}

	public ConcurrentMap<AID, Boolean> getOwnedServers() {
		return ownedServers;
	}

	public ConcurrentMap<AID, Integer> getWeightsForServersMap() {
		return weightsForServersMap;
	}

	public AID getScheduler() {
		return scheduler;
	}

	public void setScheduler(final AID scheduler) {
		this.scheduler = scheduler;
	}
}
