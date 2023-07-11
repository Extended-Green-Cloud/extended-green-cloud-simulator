package com.greencloud.application.agents.cloudnetwork;

import static com.google.common.collect.Multimaps.synchronizedMultimap;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.CNA;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
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
	protected Multimap<String, AID> serverContainers;
	protected ConcurrentMap<AID, Integer> weightsForServersMap;

	protected AtomicDouble maximumCapacity;
	protected AtomicInteger containerIndex;
	protected AID scheduler;

	AbstractCloudNetworkAgent() {
		super();

		this.agentType = CNA;
		this.serverForJobMap = new ConcurrentHashMap<>();
		this.networkJobs = new ConcurrentHashMap<>();
		this.ownedServers = new ConcurrentHashMap<>();
		this.serverContainers = synchronizedMultimap(ArrayListMultimap.create());
		this.weightsForServersMap = new ConcurrentHashMap<>();
		this.containerIndex = new AtomicInteger(0);
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

	public double getMaximumCapacity() {
		return maximumCapacity.get();
	}

	public void setMaximumCapacity(final double maximumCapacity) {
		this.maximumCapacity.set(maximumCapacity);
	}

	public Multimap<String, AID> getServerContainers() {
		return serverContainers;
	}

	public synchronized void incrementContainerIndex() {
		final int nextContainerIdx =
				containerIndex.get() == serverContainers.keySet().size() - 1 ? 0 : containerIndex.get() + 1;
		containerIndex.set(nextContainerIdx);
	}

	public AtomicInteger getContainerIndex() {
		return containerIndex;
	}
}
