package com.greencloud.application.agents.monitoring;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.monitoring.management.MonitoringWeatherManagement;
import com.greencloud.commons.agent.AgentType;

/**
 * Abstract agent class storing data of the Monitoring Agent
 */
public abstract class AbstractMonitoringAgent extends AbstractAgent {
	private final MonitoringWeatherManagement weatherManagement;
	protected double badStubProbability;

	/**
	 * Default agent constructor
	 *
	 * @apiNote weatherManagement - monitoring agent manager used to perform operations connected with com.greencloud.application.weather
	 */
	AbstractMonitoringAgent() {
		super.setup();
		this.weatherManagement = new MonitoringWeatherManagement();
		agentType = AgentType.MONITORING;
	}

	public MonitoringWeatherManagement manageWeather() {
		return weatherManagement;
	}

	public double getBadStubProbability() {
		return badStubProbability;
	}
}
