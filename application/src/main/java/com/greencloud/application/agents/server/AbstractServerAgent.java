package com.greencloud.application.agents.server;

import static com.greencloud.application.agents.server.domain.ServerAgentConstants.JOB_PROCESSING_LIMIT;
import static com.greencloud.application.agents.server.domain.ServerAgentConstants.MAX_AVAILABLE_POWER_DIFFERENCE;
import static com.greencloud.application.mapper.JsonMapper.getMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent {

	protected transient ServerStateManagement stateManagement;
	protected int initialMaximumCapacity;
	protected int currentMaximumCapacity;
	protected double pricePerHour;
	protected volatile AtomicLong currentlyProcessing;
	protected volatile ConcurrentMap<ClientJob, JobStatusEnum> serverJobs;
	protected Map<String, AID> greenSourceForJobMap;
	protected List<AID> ownedGreenSources;
	protected AID ownerCloudNetworkAgent;

	AbstractServerAgent() {
		super.setup();

		serverJobs = new ConcurrentHashMap<>();
		initialMaximumCapacity = 0;
		ownedGreenSources = new ArrayList<>();
		greenSourceForJobMap = new HashMap<>();
		currentlyProcessing = new AtomicLong(0);
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
		try {
			greenSource1 = getMapper().readValue(offer1.getContent(), GreenSourceData.class);
			greenSource2 = getMapper().readValue(offer2.getContent(), GreenSourceData.class);
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
		double powerDifference = greenSource1.getAvailablePowerInTime() - greenSource2.getAvailablePowerInTime();
		int errorDifference = (int) (greenSource1.getPowerPredictionError() - greenSource2.getPowerPredictionError());
		return (int) (errorDifference != 0 ?
				MAX_AVAILABLE_POWER_DIFFERENCE.isValidValue((long) powerDifference) ?
						errorDifference :
						powerDifference
				: powerDifference);
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

	public double getPricePerHour() {
		return pricePerHour;
	}

	public Map<ClientJob, JobStatusEnum> getServerJobs() {
		return serverJobs;
	}

	public List<AID> getOwnedGreenSources() {
		return ownedGreenSources;
	}

	public Map<String, AID> getGreenSourceForJobMap() {
		return greenSourceForJobMap;
	}

	public ServerStateManagement manage() {
		return stateManagement;
	}

	public void tookJobIntoProcessing() {
		currentlyProcessing.incrementAndGet();
	}

	public void stoppedJobProcessing() {
		currentlyProcessing.decrementAndGet();
	}

	public boolean canTakeIntoProcessing() {
		return currentlyProcessing.get() < JOB_PROCESSING_LIMIT;
	}
}
