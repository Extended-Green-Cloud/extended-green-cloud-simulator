package com.greencloud.application.agents.greenenergy;

import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.ADAPTATION_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.POWER_MANAGEMENT;
import static com.greencloud.application.domain.agent.enums.AgentManagementEnum.STATE_MANAGEMENT;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static java.util.Objects.nonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyStateManagement;
import com.greencloud.application.agents.greenenergy.management.GreenPowerManagement;
import com.greencloud.commons.agent.greenenergy.GreenEnergySourceTypeEnum;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;
import com.greencloud.commons.domain.location.Location;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;

/**
 * Abstract agent class storing data of the Green Source Energy Agent
 */
public abstract class AbstractGreenEnergyAgent extends AbstractAgent {

	protected ConcurrentMap<ServerJob, JobExecutionStatusEnum> serverJobs;
	protected Location location;
	protected GreenEnergySourceTypeEnum energyType;
	protected AID monitoringAgent;

	protected double pricePerPowerUnit;
	protected double weatherPredictionError;
	protected int maximumGeneratorCapacity;

	protected boolean hasError;

	AbstractGreenEnergyAgent() {
		super();

		this.serverJobs = new ConcurrentHashMap<>();
		this.agentType = GREEN_SOURCE;
	}

	public GreenEnergyStateManagement manage() {
		return (GreenEnergyStateManagement) agentManagementServices.get(STATE_MANAGEMENT);
	}

	public GreenPowerManagement power() {
		return (GreenPowerManagement) agentManagementServices.get(POWER_MANAGEMENT);
	}

	public GreenEnergyAdaptationManagement adapt() {
		return (GreenEnergyAdaptationManagement) agentManagementServices.get(ADAPTATION_MANAGEMENT);
	}

	public ConcurrentMap<ServerJob, JobExecutionStatusEnum> getServerJobs() {
		return serverJobs;
	}

	public double getPricePerPowerUnit() {
		return pricePerPowerUnit;
	}

	public void setPricePerPowerUnit(double pricePerPowerUnit) {
		this.pricePerPowerUnit = pricePerPowerUnit;
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

		if (nonNull(getAgentNode())) {
			((GreenEnergyAgentNode) getAgentNode()).updatePredictionError(weatherPredictionError);
		}
	}

	public GreenEnergySourceTypeEnum getEnergyType() {
		return energyType;
	}

	public int getMaximumGeneratorCapacity() {
		return maximumGeneratorCapacity;
	}

	public void setMaximumGeneratorCapacity(final int maximumGeneratorCapacity) {
		this.maximumGeneratorCapacity = maximumGeneratorCapacity;
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(final boolean hasError) {
		this.hasError = hasError;
	}
}
