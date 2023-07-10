package com.greencloud.application.agents.server;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.COMMUNICATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.SERVER;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.server.management.ServerAdaptationManagement;
import com.greencloud.application.agents.server.management.ServerCommunicationManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.AID;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent {
	protected AtomicLong currentlyProcessing;
	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> serverJobs;
	protected ConcurrentMap<AID, Integer> weightsForGreenSourcesMap;
	protected ConcurrentMap<String, AID> greenSourceForJobMap;
	protected ConcurrentMap<AID, Boolean> ownedGreenSources;
	protected AID ownerCloudNetworkAgent;

	protected int initialMaximumCapacity;
	protected int currentMaximumCapacity;
	protected boolean isDisabled;
	protected double pricePerHour;
	protected int jobProcessingLimit;
	protected String allocatedContainer;

	AbstractServerAgent() {
		super();

		serverJobs = new ConcurrentHashMap<>();
		ownedGreenSources = new ConcurrentHashMap<>();
		greenSourceForJobMap = new ConcurrentHashMap<>();
		weightsForGreenSourcesMap = new ConcurrentHashMap<>();
		currentlyProcessing = new AtomicLong(0);
		agentType = SERVER;
	}

	public ServerStateManagement manage() {
		return (ServerStateManagement) agentManagementServices.get(STATE_MANAGEMENT);
	}

	public ServerAdaptationManagement adapt() {
		return (ServerAdaptationManagement) agentManagementServices.get(ADAPTATION_MANAGEMENT);
	}

	public ServerCommunicationManagement message() {
		return (ServerCommunicationManagement) agentManagementServices.get(COMMUNICATION_MANAGEMENT);
	}

	public int getInitialMaximumCapacity() {
		return initialMaximumCapacity;
	}

	public int getCurrentMaximumCapacity() {
		return currentMaximumCapacity;
	}

	public void setCurrentMaximumCapacity(int currentMaximumCapacity) {
		this.currentMaximumCapacity = currentMaximumCapacity;
	}

	public AID getOwnerCloudNetworkAgent() {
		return ownerCloudNetworkAgent;
	}

	public ConcurrentMap<ClientJob, JobExecutionStatusEnum> getServerJobs() {
		return serverJobs;
	}

	public ConcurrentMap<AID, Boolean> getOwnedGreenSources() {
		return ownedGreenSources;
	}

	public ConcurrentMap<String, AID> getGreenSourceForJobMap() {
		return greenSourceForJobMap;
	}

	public double getPricePerHour() {
		return pricePerHour;
	}

	public ConcurrentMap<AID, Integer> getWeightsForGreenSourcesMap() {
		return weightsForGreenSourcesMap;
	}

	public void setWeightsForGreenSourcesMap(ConcurrentMap<AID, Integer> weightsForGreenSourcesMap) {
		this.weightsForGreenSourcesMap = weightsForGreenSourcesMap;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void disable() {
		isDisabled = true;
	}

	public void enable() {
		isDisabled = false;
	}

	public void tookJobIntoProcessing() {
		currentlyProcessing.incrementAndGet();
	}

	public void stoppedJobProcessing() {
		currentlyProcessing.decrementAndGet();
	}

	public boolean canTakeIntoProcessing() {
		return currentlyProcessing.get() < jobProcessingLimit && !isDisabled;
	}

	public String getAllocatedContainer() {
		return allocatedContainer;
	}
}
