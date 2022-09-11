package agents.server;

import static agents.server.domain.ServerAgentConstants.JOB_PROCESSING_LIMIT;
import static mapper.JsonMapper.getMapper;

import agents.AbstractAgent;
import agents.server.management.ServerStateManagement;

import com.fasterxml.jackson.core.JsonProcessingException;

import domain.GreenSourceData;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

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
