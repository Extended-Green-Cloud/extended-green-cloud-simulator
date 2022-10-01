package com.greencloud.application.agents.monitoring;

import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.monitoring.management.MonitoringWeatherManagement;

/**
 * Abstract agent class storing data of the Monitoring Agent
 */
public abstract class AbstractMonitoringAgent extends AbstractAgent {
	private final MonitoringWeatherManagement weatherManagement;

	/**
	 * Default agent constructor
	 *
	 * @apiNote weatherManagement - monitoring agent manager used to perform operations connected with com.greencloud.application.weather
	 */
	AbstractMonitoringAgent() {
		super.setup();
		this.weatherManagement = new MonitoringWeatherManagement();
	}

	public MonitoringWeatherManagement manageWeather() {
		return weatherManagement;
	}
}
