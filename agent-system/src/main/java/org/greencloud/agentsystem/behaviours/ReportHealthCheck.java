package org.greencloud.agentsystem.behaviours;

import static com.database.knowledge.types.DataType.HEALTH_CHECK;
import static java.lang.Long.parseLong;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;

import java.io.InputStream;
import java.util.Properties;

import org.greencloud.agentsystem.agents.EGCSAgent;
import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.utils.logger.LogBuilder;

import com.database.knowledge.domain.agent.HealthCheck;

import jade.core.behaviours.TickerBehaviour;

/**
 * Generic behaviour that reports health status to the monitoring_data database table for a given agent.
 */
public class ReportHealthCheck extends TickerBehaviour {

	private static final LogBuilder log = constructLogBuilder(ReportHealthCheck.class);
	private static final long DEFAULT_HEALTH_PERIOD = 300;
	private final EGCSAgent<?, ?, ?> myAbstractAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent agent executing the behaviour
	 */
	public ReportHealthCheck(final EGCSAgent<?, ?, ?> agent) {
		super(agent, readHealthCheckPeriod(agent));
		myAbstractAgent = agent;
	}

	private static long readHealthCheckPeriod(final EGCSAgent<?, ?, ?> agent) {
		final Properties properties = new Properties();

		try (InputStream res = ReportHealthCheck.class.getClassLoader().getResourceAsStream("config.properties")) {
			properties.load(res);
			return parseLong(properties.getProperty("reporting.health.period"));
		} catch (Exception exception) {
			log.withAgentName(agent.getLocalName())
					.messageWarn("Could not load properties file {}. Setting default health period equal {} ms.",
							exception.toString(), DEFAULT_HEALTH_PERIOD);
			return DEFAULT_HEALTH_PERIOD;
		}
	}

	/**
	 * Method reports agent's health data to the database.
	 */
	@Override
	protected void onTick() {
		if (nonNull(myAbstractAgent.getAgentNode())) {
			myAbstractAgent.getAgentNode().getDatabaseClient().writeMonitoringData(myAbstractAgent.getName(),
					HEALTH_CHECK,
					new HealthCheck(true, EGCSAgentType.valueOf(myAbstractAgent.getProperties().getAgentType())));
		}
	}
}
