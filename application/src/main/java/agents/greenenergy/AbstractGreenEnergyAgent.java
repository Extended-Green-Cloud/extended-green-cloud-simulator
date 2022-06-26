package agents.greenenergy;

import domain.job.Job;
import domain.job.JobStatusEnum;
import domain.job.PowerJob;
import domain.location.Location;
import jade.core.AID;
import jade.core.Agent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Abstract agent class storing data of the Green Source Energy Agent
 */
public abstract class AbstractGreenEnergyAgent extends Agent {

    protected int maximumCapacity;
    protected Location location;
    protected double pricePerPowerUnit;
    protected Map<PowerJob, JobStatusEnum> powerJobs;
    protected AID monitoringAgent;
    protected AID ownerServer;

    AbstractGreenEnergyAgent() {
    }

    /**
     * Abstract Green Source Energy Agent constructor
     *
     * @param maximumCapacity   maximum available power capacity of the green source
     * @param location          geographical location (longitude and latitude) of the green source
     * @param pricePerPowerUnit price for the 1 power unit (1 kWh)
     * @param powerJobs      list of power orders together with their statuses
     * @param monitoringAgent   address of the corresponding monitoring agent
     * @param ownerServer       address of the server which owns the given green source
     */
    AbstractGreenEnergyAgent(int maximumCapacity,
                             Location location,
                             double pricePerPowerUnit,
                             Map<PowerJob, JobStatusEnum> powerJobs,
                             AID monitoringAgent,
                             AID ownerServer) {
        this.maximumCapacity = maximumCapacity;
        this.location = location;
        this.pricePerPowerUnit = pricePerPowerUnit;
        this.powerJobs = powerJobs;
        this.monitoringAgent = monitoringAgent;
        this.ownerServer = ownerServer;
    }

    public int getAvailablePower(final OffsetDateTime startDate,
                                    final OffsetDateTime endDate) {
        final int powerInUser =
                powerJobs.keySet().stream()
                        .filter(job -> job.getStartTime().isBefore(endDate) &&
                                job.getEndTime().isAfter(startDate))
                        .mapToInt(PowerJob::getPower).sum();
        return maximumCapacity - powerInUser;
    }

    public PowerJob getJobById(final String jobId) {
        return powerJobs.keySet().stream().filter(job -> job.getJobId().equals(jobId)).findFirst().orElse(null);
    }

    public AID getOwnerServer() {
        return ownerServer;
    }

    public void setOwnerServer(AID ownerServer) {
        this.ownerServer = ownerServer;
    }

    public double getPricePerPowerUnit() {
        return pricePerPowerUnit;
    }

    public void setPricePerPowerUnit(double pricePerPowerUnit) {
        this.pricePerPowerUnit = pricePerPowerUnit;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public Map<PowerJob, JobStatusEnum> getPowerJobs() {
        return powerJobs;
    }

    public void setPowerJobs(Map<PowerJob, JobStatusEnum> powerJobs) {
        this.powerJobs = powerJobs;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public AID getMonitoringAgent() {
        return monitoringAgent;
    }

    public void setMonitoringAgent(AID monitoringAgent) {
        this.monitoringAgent = monitoringAgent;
    }
}
