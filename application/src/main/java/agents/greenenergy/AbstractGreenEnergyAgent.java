package agents.greenenergy;

import domain.job.Job;
import domain.location.Location;
import jade.core.AID;
import jade.core.Agent;
import java.util.Set;

public abstract class AbstractGreenEnergyAgent extends Agent {

    protected int availableCapacity;

    protected Location location;

    protected AID monitoringAgent;

    protected double pricePerPowerUnit;

    protected Set<Job> currentJobs;

    public double getPricePerPowerUnit() {
        return pricePerPowerUnit;
    }

    public void setPricePerPowerUnit(double pricePerPowerUnit) {
        this.pricePerPowerUnit = pricePerPowerUnit;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public Location getLocation() {
        return location;
    }

    public AID getMonitoringAgent() {
        return monitoringAgent;
    }

    public Set<Job> getCurrentJobs() {
        return currentJobs;
    }
}
