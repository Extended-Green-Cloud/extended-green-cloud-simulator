package org.greencloud.agentsystem.agents.monitoring;

import static java.util.Collections.singletonList;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.util.List;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.agentsystem.agents.monitoring.behaviour.ServeForecastWeather;
import org.greencloud.agentsystem.agents.monitoring.management.MonitoringWeatherManagement;
import org.greencloud.commons.args.agent.EGCSAgentControllerArgs;
import org.greencloud.commons.args.agent.monitoring.agent.MonitoringAgentProps;
import org.greencloud.commons.args.agent.monitoring.factory.MonitoringArgs;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.gui.agents.monitoring.MonitoringNode;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing weather station that is responsible for retrieving the weather and sending the data to the
 * Green Source Agent.
 */
public class MonitoringAgent extends EGCSAgent<MonitoringNode, MonitoringAgentProps, MonitoringArgs> {

	private static final LogBuilder log = constructLogBuilder(MonitoringAgent.class);

	protected MonitoringWeatherManagement weatherManagement;

	public MonitoringAgent() {
		super();
	}

	@Override
	protected void initializeAgent(final Object[] args) {
		try {
			final EGCSAgentControllerArgs<MonitoringArgs> controllerArgs = parseAgentArguments(args);
			this.properties = new MonitoringAgentProps(getName(), controllerArgs.getAgentArgs());

			this.weatherManagement = new MonitoringWeatherManagement(this);
		} catch (final Exception e) {
			log.messageError("Incorrect arguments: some parameters for Monitoring Agent are incorrect: {}.", e);
			doDelete();
		}
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return singletonList(new ServeForecastWeather(this));
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		this.weatherManagement = new MonitoringWeatherManagement(this);
	}

	public MonitoringWeatherManagement weather() {
		return weatherManagement;
	}
}
