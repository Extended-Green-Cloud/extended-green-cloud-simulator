package agents.server;

import static common.GUIUtils.displayMessageArrow;
import static common.GUIUtils.updateServerState;
import static java.time.temporal.ChronoUnit.HOURS;
import static mapper.JsonMapper.getMapper;
import static messages.domain.JobStatusMessageFactory.prepareFinishMessage;

import agents.AbstractAgent;
import agents.server.domain.ServerStateManagement;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.GreenSourceData;
import domain.job.Job;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Abstract agent class storing data of the Server Agent
 */
public abstract class AbstractServerAgent extends AbstractAgent {

    protected transient ServerStateManagement stateManagement;
    protected double pricePerHour;
    protected int maximumCapacity;
    protected transient ConcurrentMap<Job, JobStatusEnum> serverJobs;
    protected transient ConcurrentMap<String, AID> greenSourceForJobMap;
    protected List<AID> ownedGreenSources;
    protected AID ownerCloudNetworkAgent;

    AbstractServerAgent() {
        super.setup();

        serverJobs = new ConcurrentHashMap<>();
        ownedGreenSources = new ArrayList<>();
        greenSourceForJobMap = new ConcurrentHashMap<>();
    }

    /**
     * Abstract Server Agent constructor.
     *
     * @param pricePerHour           price for 1-hour server service
     * @param maximumCapacity        maximum available server power capacity
     * @param serverJobs             list of jobs together with their status that are being processed
     *                               by the server
     * @param greenSourceForJobMap   map storing jobs and corresponding job's executor addresses
     * @param ownedGreenSources      list of addresses of owned green sources
     * @param ownerCloudNetworkAgent address of the owner cloud network agent
     */
    AbstractServerAgent(double pricePerHour,
                        int maximumCapacity,
                        ConcurrentMap<Job, JobStatusEnum> serverJobs,
                        ConcurrentMap<String, AID> greenSourceForJobMap,
                        List<AID> ownedGreenSources,
                        AID ownerCloudNetworkAgent) {
        this.pricePerHour = pricePerHour;
        this.maximumCapacity = maximumCapacity;
        this.serverJobs = serverJobs;
        this.greenSourceForJobMap = greenSourceForJobMap;
        this.ownedGreenSources = ownedGreenSources;
        this.ownerCloudNetworkAgent = ownerCloudNetworkAgent;
    }

    /**
     * Method chooses the green source for job execution
     *
     * @param greenSourceOffers offers from green sources
     * @return chosen offer
     */
    public ACLMessage chooseGreenSourceToExecuteJob(final List<ACLMessage> greenSourceOffers) {
        final Comparator<ACLMessage> compareGreenSources =
                Comparator.comparingDouble(greenSource -> {
                    try {
                        return getMapper().readValue(greenSource.getContent(), GreenSourceData.class).getAvailablePowerInTime();
                    } catch (final JsonProcessingException e) {
                        return Double.MAX_VALUE;
                    }
                });
        return greenSourceOffers.stream().min(compareGreenSources).orElseThrow();
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public AID getOwnerCloudNetworkAgent() {
        return ownerCloudNetworkAgent;
    }

    public void setOwnerCloudNetworkAgent(AID ownerCloudNetworkAgent) {
        this.ownerCloudNetworkAgent = ownerCloudNetworkAgent;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public ConcurrentMap<Job, JobStatusEnum> getServerJobs() {
        return serverJobs;
    }

    public void setServerJobs(ConcurrentHashMap<Job, JobStatusEnum> serverJobs) {
        this.serverJobs = serverJobs;
    }

    public List<AID> getOwnedGreenSources() {
        return ownedGreenSources;
    }

    public void setOwnedGreenSources(List<AID> ownedGreenSources) {
        this.ownedGreenSources = ownedGreenSources;
    }

    public ConcurrentMap<String, AID> getGreenSourceForJobMap() {
        return greenSourceForJobMap;
    }

    public void setGreenSourceForJobMap(ConcurrentHashMap<String, AID> greenSourceForJobMap) {
        this.greenSourceForJobMap = greenSourceForJobMap;
    }

    public ServerStateManagement manage() {
        return stateManagement;
    }
}
