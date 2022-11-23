package com.greencloud.application.agents.greenenergy;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INITIAL_WEATHER_PREDICTION_ERROR;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.PowerJob;
import com.greencloud.commons.location.Location;

import jade.core.AID;

/**
 * Abstract agent class storing data of the Green Source Energy Agent
 */
public abstract class AbstractGreenEnergyAgent extends AbstractAgent {

	protected transient GreenPowerManagement greenPowerManagement;
	protected transient GreenEnergyStateManagement stateManagement;
	protected transient Location location;
	protected GreenEnergySourceTypeEnum energyType;
	protected double pricePerPowerUnit;
	protected volatile ConcurrentMap<PowerJob, ExecutionJobStatusEnum> powerJobs;
	protected AID monitoringAgent;
	protected AID ownerServer;
	protected double weatherPredictionError;

	AbstractGreenEnergyAgent() {
		super.setup();
		this.powerJobs = new ConcurrentHashMap<>();
		this.weatherPredictionError = INITIAL_WEATHER_PREDICTION_ERROR;
	}

	public AID getOwnerServer() {
		return ownerServer;
	}

	public double getPricePerPowerUnit() {
		return pricePerPowerUnit;
	}

	public void setPricePerPowerUnit(double pricePerPowerUnit) {
		this.pricePerPowerUnit = pricePerPowerUnit;
	}

	public Map<PowerJob, ExecutionJobStatusEnum> getPowerJobs() {
		return powerJobs;
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

	public double getWeatherPredictionError() {
		return weatherPredictionError;
	}

	public void setWeatherPredictionError(double weatherPredictionError) {
		this.weatherPredictionError = weatherPredictionError;
	}

	public void setGreenPowerManagement(GreenPowerManagement greenPowerManagement) {
		this.greenPowerManagement = greenPowerManagement;
	}

	public GreenEnergySourceTypeEnum getEnergyType() {
		return energyType;
	}

	public GreenEnergyStateManagement manage() {
		return stateManagement;
	}

	public GreenPowerManagement manageGreenPower() {
		return greenPowerManagement;
	}
}
