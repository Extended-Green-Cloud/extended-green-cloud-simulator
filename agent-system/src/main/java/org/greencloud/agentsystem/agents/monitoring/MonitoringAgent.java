package org.greencloud.agentsystem.agents.monitoring;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.greencloud.agentsystem.agents.monitoring.behaviour.ServeForecastWeather;
import org.greencloud.agentsystem.agents.monitoring.management.MonitoringWeatherManagement;
import org.slf4j.Logger;

import com.greencloud.commons.args.agent.monitoring.agent.MonitoringAgentProps;

import jade.core.behaviours.Behaviour;

/**
 * Agent representing weather station that is responsible for retrieving the weather and sending the data to the
 * Green Source Agent
 */
public class MonitoringAgent extends AbstractMonitoringAgent {
	private static final Logger logger = getLogger(MonitoringAgent.class);

	@Override
	protected void initializeAgent(final Object[] args) {
		this.properties = new MonitoringAgentProps(getName());

		if (args.length > 2 && nonNull(args[0])) {
			this.properties.setBadStubProbability(parseDouble(String.valueOf(args[0])));
		}
		readOfflineMode();
	}

	@Override
	protected List<Behaviour> prepareStartingBehaviours() {
		return singletonList(new ServeForecastWeather(this));
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		this.weatherManagement = new MonitoringWeatherManagement();
	}

	private void readOfflineMode() {
		final Properties properties = new Properties();

		try (final InputStream res = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			properties.load(res);
			this.properties.setOfflineMode(parseBoolean(properties.getProperty("offline.mode")));
		} catch (Exception exception) {
			logger.warn("Could not load properties file {}. Setting offline mode to false.", exception.toString());
		}
	}
}
