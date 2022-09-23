package com.greencloud.application.agents.server;

import static com.greencloud.application.agents.server.domain.ServerAgentConstants.JOB_PROCESSING_LIMIT;
import static com.greencloud.application.mapper.JsonMapper.getMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.server.management.ServerManagement;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent {

	protected transient ServerStateManagement stateManagement;

	protected transient ServerManagement management;
	protected int initialMaximumCapacity;
	protected int currentMaximumCapacity;
	protected double pricePerHour;
	protected volatile AtomicLong currentlyProcessing;
	protected volatile ConcurrentMap<Job, JobStatusEnum> serverJobs;
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
		final Comparator<ACLMessage> compareGreenSources =
				Comparator.comparingDouble(
						greenSource -> {
							try {
								return getMapper()
										.readValue(greenSource.getContent(), GreenSourceData.class)
										.getAvailablePowerInTime();
							} catch (final JsonProcessingException e) {
								return Double.MAX_VALUE;
							}
						});
		return greenSourceOffers.stream().min(compareGreenSources).orElseThrow();
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

	public Map<Job, JobStatusEnum> getServerJobs() {
		return serverJobs;
	}

	public List<AID> getOwnedGreenSources() {
		return ownedGreenSources;
	}

	public Map<String, AID> getGreenSourceForJobMap() {
		return greenSourceForJobMap;
	}

	public ServerStateManagement manageState() {
		return stateManagement;
	}

	public ServerManagement manage() {return management;}

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
