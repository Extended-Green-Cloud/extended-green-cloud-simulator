package agents.cloudnetwork;

import agents.AbstractAgent;
import agents.cloudnetwork.domain.CloudNetworkStateManagement;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.AID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract agent class storing the data regarding Cloud Network Agent
 */
public abstract class AbstractCloudNetworkAgent extends AbstractAgent {

    protected transient CloudNetworkStateManagement stateManagement;
    protected Map<Job, JobStatusEnum> networkJobs;
    protected Map<String, AID> serverForJobMap;
    protected List<AID> ownedServers;

    AbstractCloudNetworkAgent() {
        super.setup();
    }

    /**
     * Abstract Cloud Network Agent constructor.
     *
     * @param networkJobs     list of the jobs together with their statuses
     *                        that are being processed in the network
     * @param serverForJobMap map storing jobs and corresponding job's executor addresses
     * @param ownedServers    list of addresses of the owned servers
     */
    AbstractCloudNetworkAgent(Map<Job, JobStatusEnum> networkJobs, Map<String, AID> serverForJobMap, List<AID> ownedServers) {
        this.serverForJobMap = serverForJobMap;
        this.networkJobs = networkJobs;
        this.ownedServers = ownedServers;
    }

    /**
     * Method run on agent start. It initializes the Cloud Network Agent data with default values
     */
    @Override
    protected void setup() {
        super.setup();

        serverForJobMap = new HashMap<>();
        networkJobs = new HashMap<>();
    }

    public Map<String, AID> getServerForJobMap() {
        return serverForJobMap;
    }

    public void setServerForJobMap(Map<String, AID> serverForJobMap) {
        this.serverForJobMap = serverForJobMap;
    }

    public Map<Job, JobStatusEnum> getNetworkJobs() {
        return networkJobs;
    }

    public void setNetworkJobs(Map<Job, JobStatusEnum> networkJobs) {
        this.networkJobs = networkJobs;
    }

    public List<AID> getOwnedServers() {
        return ownedServers;
    }

    public void setOwnedServers(List<AID> ownedServers) {
        this.ownedServers = ownedServers;
    }

    public CloudNetworkStateManagement manage() {
        return stateManagement;
    }
}
