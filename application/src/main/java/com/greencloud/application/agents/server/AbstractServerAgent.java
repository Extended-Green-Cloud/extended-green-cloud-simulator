package com.greencloud.application.agents.server;

import static com.greencloud.application.agents.server.domain.ServerAgentConstants.MAX_AVAILABLE_POWER_DIFFERENCE;
import static com.greencloud.application.mapper.JsonMapper.getMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.server.management.ServerAdaptationManagement;
import com.greencloud.application.agents.server.management.ServerConfigManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ExecutionJobStatusEnum;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent {

	protected transient ServerStateManagement stateManagement;
	protected transient ServerConfigManagement configManagement;
	protected transient ServerAdaptationManagement adaptationManagement;
	protected int initialMaximumCapacity;
	protected int currentMaximumCapacity;

	protected boolean isDisabled;
	protected AtomicLong currentlyProcessing;
	protected ConcurrentMap<ClientJob, ExecutionJobStatusEnum> serverJobs;
	protected Map<String, AID> greenSourceForJobMap;
	protected Map<AID, Boolean> ownedGreenSources;
	protected AID ownerCloudNetworkAgent;

	AbstractServerAgent() {
		super.setup();

		serverJobs = new ConcurrentHashMap<>();
		initialMaximumCapacity = 0;
		ownedGreenSources = new HashMap<>();
		greenSourceForJobMap = new HashMap<>();
		currentlyProcessing = new AtomicLong(0);
		agentType = AgentType.SERVER;
	}

	/**
	 * Method chooses the green source for job execution
	 *
	 * @param greenSourceOffers offers from green sources
	 * @return chosen offer
	 */
	public ACLMessage chooseGreenSourceToExecuteJob(final List<ACLMessage> greenSourceOffers) {
		return greenSourceOffers.stream().min(this::compareGreenSourceOffers).orElseThrow();
	}

	private int compareGreenSourceOffers(final ACLMessage offer1, final ACLMessage offer2) {
		GreenSourceData greenSource1;
		GreenSourceData greenSource2;
		int weight1 = this.manageConfig().getWeightsForGreenSourcesMap().get(offer1.getSender());
		int weight2 = this.manageConfig().getWeightsForGreenSourcesMap().get(offer2.getSender());
		try {
			greenSource1 = getMapper().readValue(offer1.getContent(), GreenSourceData.class);
			greenSource2 = getMapper().readValue(offer2.getContent(), GreenSourceData.class);
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
		double powerDifference =
				greenSource2.getAvailablePowerInTime() * weight2 - greenSource1.getAvailablePowerInTime() * weight1;
		double errorDifference = (greenSource1.getPowerPredictionError() - greenSource2.getPowerPredictionError());
		int priceDifference = (int) (greenSource1.getPricePerPowerUnit() - greenSource2.getPricePerPowerUnit());

		return (int) (errorDifference != 0 ?
				Math.signum(errorDifference) :
				MAX_AVAILABLE_POWER_DIFFERENCE.isValidValue((long) powerDifference) ?
						priceDifference :
						Math.signum(powerDifference));
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

	public Map<ClientJob, ExecutionJobStatusEnum> getServerJobs() {
		return serverJobs;
	}

	public Map<AID, Boolean> getOwnedGreenSources() {
		return ownedGreenSources;
	}

	public Map<String, AID> getGreenSourceForJobMap() {
		return greenSourceForJobMap;
	}

	public boolean isDisabled() { return isDisabled; }

	public void disable() { isDisabled = true; }
	public void enable() { isDisabled = false; }

	public ServerStateManagement manage() {
		return stateManagement;
	}

	public ServerConfigManagement manageConfig() {
		return configManagement;
	}

	public ServerAdaptationManagement adapt() {
		return adaptationManagement;
	}

	public void tookJobIntoProcessing() {
		currentlyProcessing.incrementAndGet();
	}

	public void stoppedJobProcessing() {
		currentlyProcessing.decrementAndGet();
	}

	public boolean canTakeIntoProcessing() {
		return currentlyProcessing.get() < manageConfig().getJobProcessingLimit() && !isDisabled();
	}
}
