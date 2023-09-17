package org.greencloud.agentsystem.agents.monitoring;

import org.greencloud.agentsystem.agents.AbstractAgent;
import org.greencloud.agentsystem.agents.monitoring.management.MonitoringWeatherManagement;

import com.greencloud.commons.args.agent.monitoring.agent.MonitoringAgentProps;
import com.gui.agents.MonitoringNode;

/**
 * Abstract agent class storing data of the Monitoring Agent
 */
public abstract class AbstractMonitoringAgent extends AbstractAgent<MonitoringNode, MonitoringAgentProps> {

	protected MonitoringWeatherManagement weatherManagement;

	AbstractMonitoringAgent() {
		super();
		this.properties = new MonitoringAgentProps(getName());
		this.weatherManagement = new MonitoringWeatherManagement();
	}

	public MonitoringWeatherManagement weather() {
		return weatherManagement;
	}

}
