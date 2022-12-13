package com.greencloud.application.agents.greenenergy;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.database.knowledge.domain.action.AdaptationAction;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ServerJob;
import com.greencloud.commons.location.Location;
import com.greencloud.commons.managingsystem.planner.AdaptationActionParameters;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;

/**
 * Abstract agent class storing data of the Green Source Energy Agent
 */
public abstract class AbstractGreenEnergyAgent extends AbstractAgent {

	protected transient GreenPowerManagement greenPowerManagement;
	protected transient GreenEnergyStateManagement stateManagement;
	protected transient GreenEnergyAdaptationManagement adaptationManagement;
	protected Location location;
	protected GreenEnergySourceTypeEnum energyType;
	protected double pricePerPowerUnit;
	protected volatile ConcurrentMap<ServerJob, ExecutionJobStatusEnum> serverJobs;
	protected AID monitoringAgent;
	protected double weatherPredictionError;

	AbstractGreenEnergyAgent() {
		super.setup();
		this.serverJobs = new ConcurrentHashMap<>();
		agentType = AgentType.GREEN_SOURCE;
	}

	public double getPricePerPowerUnit() {
		return pricePerPowerUnit;
	}

	public void setPricePerPowerUnit(double pricePerPowerUnit) {
		this.pricePerPowerUnit = pricePerPowerUnit;
	}

	public Map<ServerJob, ExecutionJobStatusEnum> getServerJobs() {
		return serverJobs;
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

		if (Objects.nonNull(getAgentNode())) {
			((GreenEnergyAgentNode) getAgentNode()).updatePredictionError(weatherPredictionError);
		}
	}

	public void setGreenPowerManagement(GreenPowerManagement greenPowerManagement) {
		this.greenPowerManagement = greenPowerManagement;
	}

	public void setAdaptationManagement(
			GreenEnergyAdaptationManagement adaptationManagement) {
		this.adaptationManagement = adaptationManagement;
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

	@Override
	public boolean executeAction(AdaptationAction adaptationAction, AdaptationActionParameters actionParameters) {
		return switch (adaptationAction.getAction()) {
			case INCREASE_GREEN_SOURCE_ERROR -> adaptationManagement.adaptAgentWeatherPredictionError(
					(IncrementGreenSourceErrorParameters) actionParameters);
			default -> false;
		};
	}
}
